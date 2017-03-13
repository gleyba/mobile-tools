package bycicle_tool

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats._
import java.io._

import utils._
import sConfig._

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

case class ProjectConfigModel(
  name:String,
  product_name:String,
  organization:String,
  bundle:String,
  template:String,
  djinni:Option[DjinniConfigModel]
) {}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

class ProjectConfig(confFile:File) {
  implicit val formats = DefaultFormats

  val model = parse(confFile).extract[ProjectConfigModel]

  lazy val templateDir = new File(s"projects_templates/${model.template}/ios-framework")
  lazy val isDjinniSupport : Boolean = model.template == "with-djinni" && model.djinni.isDefined
}

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈
