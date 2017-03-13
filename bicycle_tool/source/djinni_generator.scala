package bycicle_tool

import sConfig._
import utils._
import java.io._

import scala.collection.mutable
import scala.language.postfixOps
import sys.process._



import org.json4s._
import org.json4s.native.JsonMethods._

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class OutFilesLine(dir:String,file:String)

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class DjinniIdlConfigModel (
  cppOutDir: Option[String] = None,
  cppNamespace: Option[String] = None,
  cppModelOutDir: Option[String] = None,
  javaOutDir: Option[String] = None,
  javaPackage: Option[String] = None,
  jniOutDir: Option[String] = None,
  objCOutDir: Option[String] = None,
  objCppOutDir: Option[String] = None,
  yamlOutDir: Option[String] = None,
  yamlSearchPaths: Option[Seq[String]] = None
) {}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class DjinniIdlModel(
  name:String,
  idlPath:String,
  config:Option[DjinniIdlConfigModel] = None
) {}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class DjinniConfigModel(
  global_config: Option[DjinniIdlConfigModel],
  write_objc_to: Option[String],
  idls: Option[List[DjinniIdlModel]]
)

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object djinni_generator {

  //----------------------------------------------------------------------

  var defaultConfig = DjinniIdlConfigModel()

  val djinniRun : String = "external_tools/djinni/src/run"

  //----------------------------------------------------------------------

  private def process(model: DjinniIdlModel): Unit = {
    val config = model.config.get

    val djinniCmd = Seq[String](
      djinniRun,
      "--java-out",config.javaOutDir.get,
      "--java-package",config.javaPackage.get,
      "--cpp-out",sDjinniGeneratedDirPath + "/" + config.cppOutDir.get,
      "--cpp-optional-template","std::experimental::optional",
      "--cpp-optional-header","<experimental/optional>",
      "--cpp-include-prefix",config.cppOutDir.get + "/",
      "--cpp-namespace",config.cppNamespace.get,
      "--jni-out",sDjinniGeneratedDirPath + "/" + config.jniOutDir.get,
      "--jni-include-prefix",config.jniOutDir.get + "/",
      "--jni-include-cpp-prefix",config.cppOutDir.get + "/",
      "--objc-out",sDjinniGeneratedDirPath + "/" + config.objCOutDir.get,
//      "--objc-include-prefix",config.objCOutDir.get + "/",
      "--objc-type-prefix","objc",
      "--objcpp-out",sDjinniGeneratedDirPath + "/" + config.objCppOutDir.get,
      "--objcpp-include-prefix",config.objCppOutDir.get + "/",
      "--objcpp-include-cpp-prefix",config.cppOutDir.get + "/",
      "--objcpp-include-objc-prefix",config.objCOutDir.get + "/",
      "--cpp-model-out",sDjinniGeneratedDirPath + "/" + config.cppModelOutDir.get,
      "--yaml-out",sDjinniGeneratedDirPath + "/" + config.yamlOutDir.get,
      "--yaml-out-file",model.name + ".yaml",
      "--yaml-search-paths",config.yamlSearchPaths.get.mkString(","),
      "--list-out-files", sDjinniGeneratedDirPath + "/" + "/djinni_list.json",
      "--idl",cur.projectDir + "/" + model.idlPath
    )

    System.out.println(djinniCmd.mkString(" "))
    djinniCmd!!
  }

  //----------------------------------------------------------------------

  def mergeIdlModels(source: DjinniIdlConfigModel, mergingOpt: Option[DjinniIdlConfigModel]) : DjinniIdlConfigModel = {
    if (!mergingOpt.isDefined) {
      return source
    } else {
      val merge = mergingOpt.get

      return source.copy(
        cppOutDir = if (merge.cppOutDir.isDefined) merge.cppOutDir else source.cppOutDir,
        cppNamespace = if (merge.cppNamespace.isDefined) merge.cppOutDir else source.cppNamespace,
        cppModelOutDir = if (merge.cppModelOutDir.isDefined) merge.cppModelOutDir else source.cppModelOutDir,
        javaOutDir = if (merge.javaOutDir.isDefined) Some(sProjectDirPath + "/" + merge.javaOutDir.get) else source.javaOutDir,
        javaPackage = if (merge.javaPackage.isDefined) merge.javaPackage else source.javaPackage,
        jniOutDir = if (merge.jniOutDir.isDefined) merge.jniOutDir else source.jniOutDir,
        objCOutDir = if (merge.objCOutDir.isDefined) merge.objCOutDir else source.objCOutDir,
        objCppOutDir = if (merge.objCppOutDir.isDefined) merge.objCppOutDir else source.objCppOutDir,
        yamlOutDir = if (merge.yamlOutDir.isDefined) merge.yamlOutDir else source.yamlOutDir,
        yamlSearchPaths = if (merge.yamlSearchPaths.isDefined)
          Some(merge.yamlSearchPaths.get.map({ x =>
            val dir = new File(cur.projectDir, x)
            assert(dir.exists())
            dir.getCanonicalPath()
          }) ++ source.yamlSearchPaths.get)
        else
          source.yamlSearchPaths
      )
    }
  }

  //----------------------------------------------------------------------

  def checkNeedGenerate(model: DjinniIdlModel): Unit = {
    if (checkModified(model.idlPath,cur.regen)) {
      process(model.copy(config = Some(mergeIdlModels(defaultConfig,model.config))))
    }
  }

  //----------------------------------------------------------------------

  def start() : Unit = {


    //generate djinni files
    if (project.isDjinniSupport) {
      val model = project.model.djinni.get

      if (sConfig.cur.regen) {
        val buildModeDir = sDjinniGeneratedDir
        deleteIfExists(buildModeDir)
        if (model.global_config.isDefined && model.global_config.get.javaOutDir.isDefined) {
            val javaOutDir = new File(sProjectDir,model.global_config.get.javaOutDir.get)
            deleteIfExists(javaOutDir)
        }
      }

      djinni_generator.defaultConfig = djinni_generator.mergeIdlModels(
        DjinniIdlConfigModel(
          cppOutDir = Some("cpp"),
          cppNamespace = Some("gen"),
          cppModelOutDir = Some("cpp_model"),
          javaOutDir = Some(sDjinniGeneratedDirPath + "java"),
          javaPackage = Some(project.model.bundle + ".gen"),
          jniOutDir = Some("jni"),
          objCOutDir = Some("objc"),
          objCppOutDir = Some("objcpp"),
          yamlOutDir = Some("yaml"),
          yamlSearchPaths = Some(Seq(sDjinniGeneratedDirPath))
        ),
        model.global_config
      )

      model.idls.get.foreach(djinni_generator.checkNeedGenerate)

      val generatedBaseDir = new File(sConfig.sBuildDir + "/generated")
      if (!generatedBaseDir.exists())
        generatedBaseDir.mkdirs()

      checkSymlinkPointsTo(generatedBaseDir.getCanonicalPath() + "/djinni",sDjinniGeneratedDirPath)

      if (model.write_objc_to.isDefined) {
        val frameworkHeader = new File(sProjectDir, model.write_objc_to.get)

        assert(frameworkHeader.exists())

        val outLists = new File(sDjinniGeneratedDirPath + "/djinni_list.json")
        assert(outLists.exists())

        implicit val formats = DefaultFormats

        val objCFilesList = new mutable.MutableList[String]()
        io.Source.fromFile(outLists).getLines()
            .map(x => parse(x).extract[OutFilesLine])
            .filter({ x =>
              x.dir == "objc" && x.file.substring(x.file.lastIndexOf('.')) != ".mm"
            })
            .foreach(objCFilesList += _.file)

        val headerLines = new mutable.MutableList[String]()
        val iter = io.Source.fromFile(frameworkHeader).getLines()
        while (iter.hasNext) {
          var l = iter.next()
          if (l.startsWith("//__IFACE_GENERATED_SECTION_START")) {
            headerLines += l
            for (objcF <- objCFilesList) {
              headerLines += s"""#import "$objcF""""
            }
            while(!l.startsWith("//__IFACE_GENERATED_SECTION_END"))
              l = iter.next()
          }
          headerLines += l
        }

        val bw = new BufferedWriter(new FileWriter(frameworkHeader))
        for(l <- headerLines) {
          bw.write(l + "\n")
        }
        bw.close()
      }
    }
  }

  //----------------------------------------------------------------------
}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈