package com.prt2121.pojo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class PojoApplication : Application() {

  override fun start(primaryStage: Stage) {
    val root = FXMLLoader.load<Parent>(javaClass.getResource("/json2pojo.fxml"))
    val scene = Scene(root, 640.0, 520.0)
    primaryStage.isResizable = false
    primaryStage.title = "json2pojo"
    primaryStage.scene = scene
    primaryStage.show()
  }
}

fun main(args: Array<String>) {
  Application.launch(PojoApplication::class.java, * arrayOf())
}