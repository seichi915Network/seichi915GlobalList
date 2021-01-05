ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "1.0.0"
ThisBuild / description := "seichi915Network グローバルリストプラグイン"

resolvers ++= Seq(
  "hub.spigotmc.org" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
  "oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots",
  "maven.elmakers.com" at "https://maven.elmakers.com/repository/",
  "papermc.io" at "https://papermc.io/repo/repository/maven-public/"
)

libraryDependencies ++= Seq(
  "net.md-5" % "bungeecord-api" % "1.16-R0.3"
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", _ @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "plugin.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "config.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "Syntax.java" => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "unwanted.txt" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "seichi915GlobalList",
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "build" / s"seichi915GlobalList-${version.value}.jar"
  )
