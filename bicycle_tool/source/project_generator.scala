package bycicle_tool

import java.io._
import utils._

object project_generator {

  val mappings = Map[String,String](
    "__PRODUCT_NAME__" -> sConfig.project.model.product_name,
    "__BUNDLE__" -> sConfig.project.model.bundle,
    "__ORGANIZATION__" -> sConfig.project.model.organization
  )

  val excludes = Set(
    "project.xcworkspace",
    "xcuserdata"
  )

  //----------------------------------------------------------------------

  def start() : Unit = {
    if (sConfig.cur.regen) {
      deleteIfExists(sConfig.sProjectDir)
    }

    djinni_generator.start()

    val iosProjDir = new File(sConfig.sProjectDir,"ios")

    copyTemplateProjs(
      sConfig.project.templateDir,
      iosProjDir
    )

    val xcodeProject = new XcodeProject(
      iosProjDir.getCanonicalPath()
        + "/" + sConfig.project.model.product_name + ".xcodeproj/project.pbxproj"
    )
    xcodeProject.readPbxproj()
  }

  //----------------------------------------------------------------------

  def copyTemplateFile(src:File,dstDir:File) : Unit = {
    var name = src.getName()
    if (name.startsWith(".")) {
      return
    }

    if (excludes.contains(name))
      return

    if (!dstDir.exists()) {
      dstDir.mkdirs()
    }

    for((k,v) <- mappings) {
      name = name.replace(k,v)
    }

    if (src.isDirectory()) {
      src
        .listFiles()
        .foreach(copyTemplateFile(_,new File(dstDir.getCanonicalPath() + "/" + name)))
      return
    }

    val writer = new BufferedWriter(new FileWriter(dstDir.getCanonicalPath() + "/" + name))

    io.Source.fromFile(src)
      .getLines
      .foreach(line => {
        var result : String = line
        for((k,v) <- mappings) {
          result = result.replace(k,v)
        }
        writer.write(result + "\n")
      })

    writer.close()
  }

  //----------------------------------------------------------------------

  def copyTemplateProjs(template:File,destination:File) : Unit = {
    val path = template.getCanonicalPath
    assert(template.exists())
    template
      .listFiles()
      .foreach(copyTemplateFile(_,destination))
  }

  //----------------------------------------------------------------------
}
