ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "version-dependency-override-force-down",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0",
    ),
    // Force cats-kernel DOWN from its natural 2.10.0 (pulled by cats-core) to 2.9.0.
    // Without this override, Coursier's highest-wins eviction would keep 2.10.0.
    // dependencyOverrides beats eviction: the resolved version is 2.9.0 even though
    // cats-core 2.10.0 declares a dependency on cats-kernel 2.10.0.
    dependencyOverrides += "org.typelevel" %% "cats-kernel" % "2.9.0",
  )