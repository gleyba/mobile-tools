package bycicle_tool

import java.io._
import java.nio.file.Files

import sConfig._

object utils {

  //----------------------------------------------------------------------

  def exitWithError(e: String) = {
    System.err.println(e)
    System.exit(1);
  }

  //----------------------------------------------------------------------

  def deleteIfExists(file: File) : Unit = {
    if (!file.exists)
      return
    if (file.isDirectory)
      file.listFiles.foreach(deleteIfExists)
    if (file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  //----------------------------------------------------------------------

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  //----------------------------------------------------------------------

  def recursiveLatestModified(file: File): Long = {
    var lastModified : Long = 0
    if (file.isDirectory) {
      recursiveListFiles(file).foreach{ f =>
        if (lastModified < f.lastModified()) {
          lastModified = f.lastModified()
        }
      }
    } else if (file.exists) {
      lastModified = file.lastModified()
    }
    lastModified
  }

  //----------------------------------------------------------------------

  def checkModified(path: String, force: Boolean) : Boolean = {
    return force || db_utils.checkModified(path)
  }

  //----------------------------------------------------------------------

  def checkSymlinkPointsTo(linkLocationPath:String,needPointToPath:String): Unit = {

    var needRelink = false

    val needPointTo = new File(needPointToPath)
    val linkLocation = new File(linkLocationPath)

    if (linkLocation.exists()) {
      if (linkLocation.getCanonicalPath() != needPointTo.getCanonicalPath()) {
        linkLocation.delete()
        needRelink = true
      }
    } else {
      needRelink = true
    }

    if (needRelink) {
      Files.createSymbolicLink(linkLocation.getCanonicalFile.toPath(),needPointTo.getCanonicalFile.toPath())
    }

  }

  //----------------------------------------------------------------------

}
