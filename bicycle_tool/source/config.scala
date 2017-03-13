package bycicle_tool

import java.io.File

import jdk.nashorn.internal.runtime.Debug

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object Modes extends Enumeration {
  type Modes = Value
  val Debug,Release,Testing = Value
}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object Command extends Enumeration {
  type Command = Value
  val GEN_PROJECT,UPDATE_PROJECT = Value
}


//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class Config(
  command:Option[Command.Value] = None,
  initialDir:String = "",
  projectDir:String = "",
  projectConfig:String = "",
  mode:Modes.Value = Modes.Debug,
  regen:Boolean = false
) {

}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object sConfig {

  val onCloseCallbacks = new scala.collection.mutable.MutableList[()=>Unit]

  private var sCurConfig : Config = null
  private var sCurProject:ProjectConfig = null

  def cur = sCurConfig
  def project:ProjectConfig = sCurProject

  //----------------------------------------------------------------------

//  def sModeTag = sCurConfig.mode match {
//    case Modes.Debug => "Debug"
//    case Modes.Testing => "Testing"
//    case Modes.Release => "Release"
//  }

  //----------------------------------------------------------------------

  private def createDirIfNotExists(f : File): String = {
    if (!f.exists()) {
      f.mkdirs()
    }
    return f.getCanonicalPath()
  }

  //----------------------------------------------------------------------

  def set(config : Config, projectConfig: ProjectConfig) : Unit = {
    sCurConfig = config
    sCurProject = projectConfig
  }

  //----------------------------------------------------------------------

  lazy val sProjectDir = new File(cur.projectDir + "/project")
  lazy val sBuildDir = new File(cur.projectDir + "/build")
//  lazy val sBuildModeDir = new File(sBuildDir,sModeTag)
  lazy val sDBDir = new File(sBuildDir,"db")
  lazy val sGeneratedCodeDir = new File(sBuildDir,"generated")
  lazy val sDjinniGeneratedDir = new File(sGeneratedCodeDir,"djinni")

  lazy val sProjectDirPath = createDirIfNotExists(sProjectDir)
  lazy val sBuildDirPath = createDirIfNotExists(sBuildDir)
//  lazy val sBuildModeDirPath = createDirIfNotExists(sBuildModeDir)
  lazy val sDBDirPath = createDirIfNotExists(sDBDir)
  lazy val sGeneratedCodeDirPath = createDirIfNotExists(sGeneratedCodeDir)
  lazy val sDjinniGeneratedDirPath = createDirIfNotExists(sDjinniGeneratedDir)
}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈