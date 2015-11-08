package com.prt2121.pojo.rx

/**
 * Created by pt2121 on 11/7/15.
 */
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import rx.Observable
import rx.Subscriber
import rx.functions.Action0

object ObservableValueSource {

  fun <T> fromObservableValue(fxObservable: ObservableValue<T>): Observable<T> {
    return Observable.create(object : Observable.OnSubscribe<T> {
      override fun call(subscriber: Subscriber<in T>) {
        subscriber.onNext(fxObservable.value)

        val listener = object : ChangeListener<T> {
          override fun changed(observableValue: ObservableValue<out T>, prev: T, current: T) {
            subscriber.onNext(current)
          }
        }

        fxObservable.addListener(listener)

        subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(object : Action0 {
          override fun call() {
            fxObservable.removeListener(listener)
          }
        }))

      }
    })
  }

}
