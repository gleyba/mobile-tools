package bycicle_tool

import utils._

import scala.io.Source
import java.io._

import scala.collection.mutable
import scala.util.parsing.combinator._


//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

class XcodeProject(path : String) extends BaseParser {

  class LineRef {
    var data: String = ""
    var next: LineRef = null
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  abstract class ProjElem {
    var startLine:LineRef = null
    var endLine:LineRef = null

    def writeElem() : Unit
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  abstract class Section[E <: ProjElem] {
    var startLine:LineRef = null
    var endLine:LineRef = null

    val elemsList = new mutable.MutableList[E]

    def checkNeedParse(): Unit = {
      if (currentParsingLine.startsWith(s"/* Begin ${name} section */")) {
        startLine = rawLinesTail
        assert(advanceNextLine())
        while (!currentParsingLine.startsWith(s"/* End ${name} section */")) {
          val elem = getElem()
          parse(elemParser,elem.data) match {
            case Success(matched, _) => {
              matched.startLine = elem.startLine
              matched.endLine = elem.endLine
              elemsList += matched
            }
          }
        }
        endLine = rawLinesTail
      }
    }

    case class ElemData(data: String,
                        startLine:LineRef,
                        endLine:LineRef
                       )

    def name : String
    def getElem() : ElemData
    def elemParser:Parser[E]
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  class PBXBuildFile( uuid: String,
                           fname: String,
                           buildPhase: String,
                           isa: String,
                           fileRef: FileRef,
                           settings: Option[Settings]) extends ProjElem {

    //----------------------------------------------------------------------

    override def writeElem() = {
      startLine = new LineRef
      endLine = startLine
      startLine.data = s"\t\t$uuid /* $fname in $buildPhase */ = {"
      startLine.data += s"isa = $isa; "
      startLine.data += s"fileRef = ${fileRef.uuid} /* ${fileRef.fileName} */; "
      if (settings.isDefined) {
        startLine.data += "settings = {"
        if (settings.get.attributes.size > 0) {
          startLine.data += "ATTRIBUTES = ("
          for (attr <- settings.get.attributes) {
            startLine.data += s"${attr}, "
          }
          startLine.data += ");"
        }
        startLine.data += "}; "
      }
      startLine.data += "};"
    }

    //----------------------------------------------------------------------
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  class PBXBuildFileSection extends Section[PBXBuildFile] {
    override def name = "PBXBuildFile"

    //----------------------------------------------------------------------

    override def getElem() = {
      val result = ElemData(currentParsingLine, rawLinesTail,rawLinesTail)
      assert(advanceNextLine())
      result
    }

    //----------------------------------------------------------------------

    override def elemParser = uuid ~ commented(fileName ~ "in" ~ ident)  ~ "=" ~ brased(isa ~ fileRef ~ opt(settings)) ^^ {
      case uuid ~ (fileName ~ "in" ~ buildPhase)  ~ "=" ~ (isa ~ fileRef ~ settings)
      => {
        assert(fileRef.fileName == fileName)
        new PBXBuildFile(uuid,fileName,buildPhase,isa,fileRef,settings)
      }
    }


    //----------------------------------------------------------------------
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  class PBXFileReference(
                        uuid: String,
                        fname: String,
                        isa: String,
                        refFileType: RefFileType,
                        name: Option[String],
                        includeIndex: Option[String],
                        path: String,
                        sourceTree: String
                        ) extends ProjElem {

    //----------------------------------------------------------------------

    override def writeElem() = {
      startLine = new LineRef
      endLine = startLine
      startLine.data = s"\t\t$uuid /* $fname */ = {"
      startLine.data += s"isa = $isa; "
      startLine.data += s"${refFileType.fileType} = ${refFileType.value}; "
      if (name.isDefined)
        startLine.data += s"name = ${name.get}; "
      if (includeIndex.isDefined)
        startLine.data += s"includeInIndex = ${includeIndex.get}; "
      startLine.data += "path = " + path + "; "
      startLine.data += "sourceTree = " + sourceTree + "; "
      startLine.data += "};"
    }

    //----------------------------------------------------------------------
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  class PBXFileReferenceSection extends Section[PBXFileReference] {
    override def name = "PBXFileReference"

    //----------------------------------------------------------------------

    override def getElem() = {
      val result = ElemData(currentParsingLine, rawLinesTail,rawLinesTail)
      assert(advanceNextLine())
      result
    }

    //----------------------------------------------------------------------

    override def elemParser = uuid ~ commented(fileName) ~ "=" ~ brased(isa ~ refFileType ~ opt(keyValue("name",fileName)) ~ opt(keyValue("includeInIndex",numeric)) ~ keyValue("path",path) ~ keyValue("sourceTree",sourceTree)) ^^ {
      case uuid ~ (fileName) ~ "=" ~ (isa ~ fType ~ name ~ includeIndex ~ path ~ sourceTree)
      => {
        if (name.isDefined)
          assert(fileName == name.get)
        new PBXFileReference(uuid,fileName,isa,fType,name,includeIndex,path,sourceTree)
      }
    }

    //----------------------------------------------------------------------
  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈
//
//  class PBXHeadersBuildPhase (
//
//                             ) extends ProjElem {
//
//  }

  //≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

  val pbxBuildFileSection = new PBXBuildFileSection
  val pbxFileReferenceSection = new PBXFileReferenceSection

  val rawLinesHead = new LineRef
  var rawLinesTail = rawLinesHead

  var currentParsingIterator : Iterator[String] = null
  var currentParsingLine : String = null

  //----------------------------------------------------------------------

  private def advanceNextLine() : Boolean = {
    rawLinesTail.data = currentParsingLine
    rawLinesTail.next = new LineRef
    rawLinesTail = rawLinesTail.next

    if (currentParsingIterator.hasNext) {
      currentParsingLine = currentParsingIterator.next()
      return true
    }
    return false
  }

  //----------------------------------------------------------------------

  def readPbxproj() : Unit = {

    currentParsingIterator = Source.fromFile(new File(path)).getLines()
    while (advanceNextLine()) {

      pbxBuildFileSection.checkNeedParse()
      pbxFileReferenceSection.checkNeedParse()
    }
  }

  //----------------------------------------------------------------------

  def isa: Parser[String] = "isa = " ~ ident ~ ";" ^^ { case "isa = " ~ ident ~ ";" => ident }
  def sourceTree: Parser[String] = """[A-Za-z_<>]*""".r

  //----------------------------------------------------------------------

  val existingUUIDS = mutable.Set[String]()

  def uuid: Parser[String] = """[A-F0-9]+""".r ^^ {case uuid => {
    existingUUIDS += uuid
    uuid
  }}

  def genUuid: String = {
    val result = java.util.UUID.randomUUID.toString.substring(0,24)
    if (existingUUIDS.contains(result)) {
      return genUuid
    } else {
      existingUUIDS += result
      return result
    }
  }

  //----------------------------------------------------------------------

  case class FileRef(uuid:String,fileName:String)
  def fileRef: Parser[FileRef] = "fileRef = " ~ uuid ~ commented(fileName) ~ ";" ^^ {
    case "fileRef = " ~ uuid ~ (fileName) ~ ";" => FileRef(uuid,fileName)
  }

  //----------------------------------------------------------------------

  case class Settings(attributes:Seq[String])

  private def settingsAttributes:Parser[Seq[String]] =
    "ATTRIBUTES = " ~ parens(rep1sepend[String,String](ident,",")) ~ ";" ^^ {
      case "ATTRIBUTES = " ~ attrs ~ ";" => attrs
    }

  private def settings: Parser[Settings] =
    "settings = " ~ brased(settingsAttributes) ~ ";" ^^ {
      case "settings = " ~ attrs ~ ";" => new Settings(attrs)
    }

  //----------------------------------------------------------------------

  def keyValuePair(key: Parser[String],inner: Parser[String]): Parser[(String,String)] =
    key ~ "=" ~ maybeQuoted(inner) ~ ";" ^^ {case key ~ "=" ~ value ~ ";" => (key,value)}

  def keyValue(key: Parser[String],inner: Parser[String]): Parser[String]
    = keyValuePair(key,inner) ^^ {case (k,v) => v}

  //----------------------------------------------------------------------

  case class RefFileType(fileType: String, value: String)

  def refFileType: Parser[RefFileType] = keyValuePair("lastKnownFileType" | "explicitFileType","""[a-z.]*"""r) ^^ {
    case (k,v) => RefFileType(k,v)
  }

  //----------------------------------------------------------------------
}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈