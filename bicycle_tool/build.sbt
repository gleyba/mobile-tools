import com.typesafe.sbt.SbtStartScript

name := "bicycle_tool"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.json4s" %% "json4s-native" % "3.5.0"
)

scalaSource in Compile := baseDirectory.value / "source"

sourcesInBase := false

// 143 chars is the filename length limit in eCryptfs, commonly used in linux distros to encrypt homedirs.
// Make scala respect that limit via max-classfile-name, or compilation fails.
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xmax-classfile-name", "143")

seq(SbtStartScript.startScriptForClassesSettings: _*)