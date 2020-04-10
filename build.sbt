name := "bomz"
organization := "com.github.jw3"
scalaVersion := "2.13.1"
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

val zioVersion = "1.0.0-RC18-2"
val scalatest = "3.1.1"
libraryDependencies := Seq(
  "dev.zio" %% "zio" % zioVersion,
  // ------------- test
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "org.scalactic" %% "scalactic" % scalatest % Test,
  "org.scalatest" %% "scalatest" % scalatest % Test
)

enablePlugins(GitVersioning, JavaServerAppPackaging)

Compile / unmanagedResourceDirectories += baseDirectory.value / "assets"
