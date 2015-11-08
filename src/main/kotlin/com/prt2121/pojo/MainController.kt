package com.prt2121.pojo

import com.prt2121.pojo.rx.JavaFxObservable
import com.sun.codemodel.JCodeModel
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import org.jsonschema2pojo.*
import org.jsonschema2pojo.rules.RuleFactory
import java.io.File
import java.io.PrintWriter
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

  val codeModel = JCodeModel()
  val mapper = SchemaMapper(RuleFactory(object : DefaultGenerationConfig() {
    public override fun getSourceType(): SourceType {
      return SourceType.JSON
    }
  }, GsonAnnotator(), SchemaStore()), SchemaGenerator())

  override fun initialize(url: URL?, bundle: ResourceBundle?) {
    val jsonObservable = JavaFxObservable
        .fromObservableValue(jsonTextArea!!.textProperty())
        .doOnNext { generateButton?.isDisable == it.isEmpty() }
        .filter { it.isNotBlank() }

    val classNameObservable = JavaFxObservable
        .fromObservableValue(classNameField!!.textProperty())
        .doOnNext { generateButton?.isDisable == it.isEmpty() }
        .filter { it.isNotBlank() }

    val packageNameObservable = JavaFxObservable
        .fromObservableValue(packageNameField!!.textProperty())

    val clicks = JavaFxObservable
        .fromNodeEvents(generateButton!!, ActionEvent.ACTION)
        .throttleLast(3, TimeUnit.SECONDS)

    val jsonInputs = jsonObservable.zipWith(clicks) { json, click ->
      json
    }

    rx.Observable.combineLatest(jsonInputs, packageNameObservable, classNameObservable) {
      json, packageName, className ->
      val dir = File("json2pojoOutput")
      removeOldOutput(dir)
      dir.mkdir()
      val inputFile = createInputFile(json)
      mapper.generate(codeModel, className, packageName, inputFile.toURI().toURL())
      codeModel.build(dir)
      inputFile.deleteOnExit()
      dir
    }.subscribe({
      file ->
      println(file.absolutePath)
    }, {
      t ->
      print(t.message)
    })
  }

  // https://github.com/joelittlejohn/jsonschema2pojo/issues/255
  private fun createInputFile(json: String): File {
    val inputFile = File("tmp.json")
    inputFile.createNewFile()
    val out = PrintWriter(inputFile)
    out.print(json)
    out.close()
    return inputFile
  }

  private fun removeOldOutput(targetDirectory: File) {
    fun delete(f: File) {
      if (f.isDirectory) {
        for (child in f.listFiles()!!) {
          delete(child)
        }
      }
      f.delete()
    }
    if (targetDirectory.exists()) {
      for (f in targetDirectory.listFiles()!!) {
        delete(f)
      }
    }
  }

}