package com.prt2121.pojo.rx

/**
 * Created by pt2121 on 11/7/15.
 */
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventType
import javafx.scene.Node
import rx.Observable

enum class JavaFxObservable {
  ; // no instances

  companion object {
    // no instances

    fun <T : Event> fromNodeEvents(node: Node, eventType: EventType<T>): Observable<T> {
      return NodeEventSource.fromNodeEvents(node, eventType)
    }

    fun <T> fromObservableValue(fxObservable: ObservableValue<T>): Observable<T> {
      return ObservableValueSource.fromObservableValue(fxObservable)
    }
  }
}