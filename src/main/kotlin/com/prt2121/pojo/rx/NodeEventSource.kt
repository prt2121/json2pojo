package com.prt2121.pojo.rx

/**
 * Created by pt2121 on 11/7/15.
 */
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import rx.Observable
import rx.Subscriber
import rx.functions.Action0

object NodeEventSource {

  fun <T : Event> fromNodeEvents(source: Node, eventType: EventType<T>): Observable<T> {

    return Observable.create(object : Observable.OnSubscribe<T> {
      override fun call(subscriber: Subscriber<in T>) {
        val handler = object : EventHandler<T> {
          override fun handle(t: T) {
            subscriber.onNext(t)
          }
        }

        source.addEventHandler(eventType, handler)

        subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(object : Action0 {
          override fun call() {
            source.removeEventHandler(eventType, handler)
          }
        }))
      }

    }).subscribeOn(JavaFxScheduler.instance)
  }
}