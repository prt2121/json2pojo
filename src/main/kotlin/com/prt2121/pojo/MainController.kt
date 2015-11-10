package com.prt2121.pojo

import com.prt2121.pojo.rx.JavaFxObservable
import com.sun.codemodel.JCodeModel
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import org.jsonschema2pojo.*
import org.jsonschema2pojo.rules.RuleFactory
import java.io.*
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by pt2121 on 11/7/15.
 */
public class MainController : Initializable {

  @FXML var jsonTextArea: TextArea? = null
  @FXML var packageNameField: TextField? = null
  @FXML var generateButton: Button? = null
  @FXML var classNameField: TextField? = null
  @FXML var progressIndicator: ProgressIndicator? = null

  override fun initialize(url: URL?, bundle: ResourceBundle?) {
    // for logging after packaging
    System.setOut(PrintStream(object : OutputStream() {
      override fun write(b: Int) {
        appendText(b.toChar().toString())
      }
    }, true))

    val jsonObservable = JavaFxObservable
        .fromObservableValue(jsonTextArea!!.textProperty())
        .throttleLast(1, TimeUnit.SECONDS)

    val classNameObservable = JavaFxObservable
        .fromObservableValue(classNameField!!.textProperty())
        .throttleLast(1, TimeUnit.SECONDS)

    val packageNameObservable = JavaFxObservable
        .fromObservableValue(packageNameField!!.textProperty())
        .throttleLast(1, TimeUnit.SECONDS)

    val clicks = JavaFxObservable
        .fromNodeEvents(generateButton!!, MouseEvent.MOUSE_CLICKED)
        .throttleLast(1, TimeUnit.SECONDS)

    rx.Observable.combineLatest(jsonObservable, packageNameObservable, classNameObservable) {
      json, packageName, className ->
      Input(json, packageName, className)
    }.doOnNext {
      Platform.runLater {
        generateButton?.setDisable(it.json.isBlank() || it.className.isBlank())
      }
    }.subscribe()

    clicks.doOnNext {
      Platform.runLater {
        progressIndicator!!.isVisible = true
      }
    }.map {
      val dir = File("json2pojoOutput")
      val success = if(dir.exists()) dir.deleteRecursively() else true
      dir to success
    }.filter { it.second }.map {
      it.first
    }.map {
      dir ->
      dir.mkdir()
      val packageName = packageNameField?.textProperty()?.value ?: ""
      val input = Input(jsonTextArea!!.textProperty().value, packageName, classNameField!!.textProperty().value)
      val inputFile = createInputFile(input.json)
      val codeModel = JCodeModel()
      val mapper = SchemaMapper(RuleFactory(object : DefaultGenerationConfig() {
        public override fun getSourceType(): SourceType {
          return SourceType.JSON
        }
      }, GsonAnnotator(), SchemaStore()), SchemaGenerator())
      mapper.generate(codeModel, input.className, input.packageName, inputFile.toURI().toURL())
      codeModel.build(dir)
      inputFile.delete()
      println("\n")
      dir
    }.subscribe({
      file ->
      println("\n")
      println(file.name)
      println(file.absolutePath)
      Platform.runLater {
        progressIndicator!!.isVisible = false
      }
    }, {
      t ->
      println(t.message)
      Platform.runLater {
        progressIndicator!!.isVisible = false
      }
    }, {
      Platform.runLater {
        progressIndicator!!.isVisible = false
      }
    })

  }

  // https://github.com/joelittlejohn/jsonschema2pojo/issues/255
  private fun createInputFile(json: String): File {
    val inputFile = File("json2pojoOutput/tmp.json")
    inputFile.createNewFile()
    val out = PrintWriter(inputFile)
    out.print(json)
    out.close()
    return inputFile
  }

  class Input(val json: String, val packageName: String, val className: String) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other?.javaClass != javaClass) return false

      other as Input

      if (json != other.json) return false
      if (packageName != other.packageName) return false
      if (className != other.className) return false

      return true
    }

    override fun hashCode(): Int {
      var result = json.hashCode()
      result += 31 * result + packageName.hashCode()
      result += 31 * result + className.hashCode()
      return result
    }
  }

  fun appendText(str: String) {
    Platform.runLater { jsonTextArea!!.appendText(str) }
  }
}