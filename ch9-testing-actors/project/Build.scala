import sbt._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys._

object Chapter9Build extends Build {
    lazy val root = Project(id = "chapter9-testing-actors",
                            base = file("."),
                            settings = Project.defaultSettings ++ multiJvmSettings) configs(MultiJvm)

    lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((_, testResults), (_, multiJvmResults))  =>
          val results = testResults ++ multiJvmResults
          (Tests.overall(results.values), results)
    })

}

