package com.prt2121.pojo.rx

/**
 * Created by pt2121 on 11/7/15.
 */
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.util.Duration
import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import rx.subscriptions.BooleanSubscription
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions

import java.util.concurrent.TimeUnit

import java.lang.Math.max

class JavaFxScheduler internal constructor() : Scheduler() {

  override fun createWorker(): Worker {
    return InnerJavaFxScheduler()
  }

  private class InnerJavaFxScheduler : Worker() {

    private val innerSubscription = CompositeSubscription()

    override fun unsubscribe() {
      innerSubscription.unsubscribe()
    }

    override fun isUnsubscribed(): Boolean {
      return innerSubscription.isUnsubscribed
    }

    override fun schedule(action: Action0, delayTime: Long, unit: TimeUnit): Subscription {
      val s = BooleanSubscription.create()

      val delay = unit.toMillis(max(delayTime, 0))
      val timeline = Timeline(KeyFrame(Duration.millis(delay.toDouble()), object : EventHandler<ActionEvent> {

        override fun handle(event: ActionEvent) {
          if (innerSubscription.isUnsubscribed || s.isUnsubscribed) {
            return
          }
          action.call()
          innerSubscription.remove(s)
        }
      }))

      timeline.cycleCount = 1
      timeline.play()

      innerSubscription.add(s)

      return Subscriptions.create(object : Action0 {

        override fun call() {
          timeline.stop()
          s.unsubscribe()
          innerSubscription.remove(s)
        }

      })
    }

    override fun schedule(action: Action0): Subscription {
      val s = BooleanSubscription.create()
      Platform.runLater(object : Runnable {
        override fun run() {
          if (innerSubscription.isUnsubscribed || s.isUnsubscribed) {
            return
          }
          action.call()
          innerSubscription.remove(s)
        }
      })

      innerSubscription.add(s)
      return Subscriptions.create(object : Action0 {

        override fun call() {
          s.unsubscribe()
          innerSubscription.remove(s)
        }

      })
    }

  }

  companion object {
    val instance = JavaFxScheduler()
  }
}