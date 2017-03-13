package bycicle_tool

import java.io._
import scala.language.postfixOps
import sys.process._

import bycicle_tool.utils.exitWithError

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object Main {

  //----------------------------------------------------------------------

  def main(args: Array[String]): Unit = {
    val argParser = new scopt.OptionParser[Config]("bycicle_tool") {
      override def showUsageOnError = true
      help("help")

      opt[String]('d',"project_dir")
        .valueName("dir")
        .action( (x,c) =>
          if (x.startsWith("/") && new File(x).exists()) {
            c.copy(projectDir=x)
          } else if (new File(c.initialDir + "/" + x).exists()) {
            c.copy(projectDir=c.initialDir + "/" + x)
          } else {
            failure("Project dir not found")
            c.copy(projectDir=x)
          }
        )
        .text("Target project directory")

      opt[String]('p',"project_file")
        .valueName("Project file name")
        .required()
        .action( (x,c) =>
          c.copy(projectConfig=x)
        )
        .text("Project configuration file")

      opt[String]('m',"build mode")
        .valueName("<testing|debug|release>")
        .validate( x =>
          if (x.toUpperCase == "TESTING"
            || x.toUpperCase == "DEBUG"
            || x.toUpperCase == "RELEASE"
          )
            success
          else
            failure("-m must be testing, debug or release")
        )
        .action( (x,c) =>
          if (x.toUpperCase == "TESTING")
            c.copy(mode = Modes.Testing)
          else if (x.toUpperCase == "DEBUG")
            c.copy(mode = Modes.Debug)
          else if (x.toUpperCase == "RELEASE")
            c.copy(mode = Modes.Release)
          else
            c
        )
        .text("Target build modes")

      note("\nCommands:\n")

      cmd("gen")
        .action( (_, c) => c.copy(command = Some(Command.GEN_PROJECT)))
        .text("generate project")
        .children(
          opt[Unit]("force-regen")
            .action( (_, c) => c.copy(regen = true) )
            .text("forcefully regenerate project")
        )
      cmd("update")
        .action( (_, c) => c.copy(command = Some(Command.UPDATE_PROJECT)))
        .text("generate project")
        .children(
          opt[Unit]("force-regen")
            .action( (_, c) => c.copy(regen = true) )
            .text("forcefully regenerate project")
        )
    }

    argParser.parse(args.slice(1,args.size), Config(initialDir=args(0),projectDir=args(0))) match {
      case Some(config) => {
        config.command match  {
          case None => {
              System.out.print("No command set\n")
              argParser.showUsage()
              System.exit(1)
          }
          case Some(cmd) => {
            start(config,cmd)
          }
        }
      }
      case None =>
        System.exit(1); return
    }
  }

  //----------------------------------------------------------------------

  def start(config:Config,cmd: Command.Value) : Unit = {
    val projectConfig = new File(config.projectDir + "/" + config.projectConfig)
    if (!projectConfig.exists()) {
      exitWithError(s"No ${projectConfig.getCanonicalPath} project config file exists" )
      return
    }

//    "git submodule init"!!
//    ;
//    "git submodule update"!!

    sConfig.set(config,new ProjectConfig(projectConfig))

    cmd match {
      case Command.GEN_PROJECT => project_generator.start()
      case Command.UPDATE_PROJECT => djinni_generator.start()
    }
  }

  //----------------------------------------------------------------------



  //----------------------------------------------------------------------
}