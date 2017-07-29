import com.sun.jna.Platform

val compileNative = taskKey[Unit]("Compile cpp into shared library.")

lazy val root = (project in file(".")).settings(
  name := "annoy4s",
  version := "0.4.0",
  scalaVersion := "2.11.11",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  libraryDependencies ++= Seq(
    "com.github.pathikrit" %% "better-files" % "2.17.1",
    "net.java.dev.jna" % "jna" % "4.2.2",
    "org.slf4s" %% "slf4s-api" % "1.7.25",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.14" % "test"
  ),
  fork := true,
  organization := "com.gilt",
  licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"),
  homepage := Some(url("https://github.com/pishen/annoy4s")),
  pomExtra := (
    <scm>
      <url>https://github.com/pishen/annoy4s.git</url>
      <connection>scm:git:git@github.com:pishen/annoy4s.git</connection>
    </scm>
    <developers>
      <developer>
        <id>pishen</id>
        <name>Pishen Tsai</name>
      </developer>
    </developers>
  ),
  compileNative := {
    val libDir = file(s"src/main/resources/${Platform.RESOURCE_PREFIX}")
    if (!libDir.exists) {
      libDir.mkdirs()
    }
    val lib = libDir / (if (Platform.isMac) "libannoy.dylib" else "libannoy.so")
    val source = file("src/main/cpp/annoyjava.cpp")
    val cmd = s"g++ -o ${lib.getAbsolutePath} -shared ${if (Platform.isMac) "-dynamiclib" else "-fPIC"} ${source.getAbsolutePath}"
    println(cmd)
    import scala.sys.process._
    cmd.!
  }
)

// Release configuration
publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishMavenStyle := false

resolvers := Seq(
  DefaultMavenRepository,
  Resolver.typesafeRepo("releases")
)

publishTo := {
  val nexus = "https://nexus.gilt.com/nexus/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/gilt.snapshots")
  }
  else {
    Some("releases" at nexus + "content/repositories/internal-releases/")
  }
}

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.gilt.com", "publisher", "/Publish3r!")
