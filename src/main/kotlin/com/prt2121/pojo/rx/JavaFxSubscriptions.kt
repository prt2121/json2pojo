package com.prt2121.pojo.rx

/**
 * Created by pt2121 on 11/7/15.
 */
import javafx.application.Platform
import rx.Subscription
import rx.functions.Action0
import rx.subscriptions.Subscriptions

object JavaFxSubscriptions {

  fun unsubscribeInEventDispatchThread(unsubscribe: Action0): Subscription {
    return Subscriptions.create(object : Action0 {
      override fun call() {
        if (Platform.isFxApplicationThread()) {
          unsubscribe.call()
        } else {
          val inner = JavaFxScheduler.instance.createWorker()
          inner.schedule(object : Action0 {
            override fun call() {
              unsubscribe.call()
              inner.unsubscribe()
            }
          })
        }
      }
    })
  }
}