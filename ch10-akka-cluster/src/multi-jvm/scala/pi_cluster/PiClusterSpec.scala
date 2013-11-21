package pi_cluster

import language.postfixOps
import scala.concurrent.duration._

import com.typesafe.config.ConfigFactory

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers

import akka.actor.Props
import akka.cluster.Cluster
import akka.actor.Actor
import akka.testkit.TestActorRef
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender

object PiClusterSpecConfig extends MultiNodeConfig {
  // register the named roles (nodes) of the test
  val frontend1 = role("frontend1")
  val backend1 = role("backend1")
  val backend2 = role("backend2")

  // this configuration will be used for all nodes
  // note that no fixed host names and ports are used
  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    # don't use sigar for tests, native lib not in path
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector
    """))

  nodeConfig(frontend1)(
    ConfigFactory.parseString("akka.cluster.roles =[frontend]"))

  nodeConfig(backend1, backend2)(
    ConfigFactory.parseString("akka.cluster.roles =[backend]"))
}

// need one concrete test class per node
class PiClusterSpecMultiJvmNode1 extends PiClusterSpec
class PiClusterSpecMultiJvmNode2 extends PiClusterSpec
class PiClusterSpecMultiJvmNode3 extends PiClusterSpec

abstract class PiClusterSpec extends MultiNodeSpec(PiClusterSpecConfig)
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with ImplicitSender {

  import scala.util.Random._
  import PiClusterSpecConfig._

  override def initialParticipants = roles.size

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()

  "The compute `Pi` service" must {
    "start first frontend" in within(15 seconds) {
      runOn(frontend1) {
        // this will only run on the 'first' node
        Cluster(system) join node(frontend1).address
        val piFrontend = system.actorOf(Props[PiFrontend], name = "frontend")
        piFrontend ! CalculatePi(10000, 10000)
        expectMsgPF() {
          // no backends yet, service unavailble
          case CalculatePiFailed(_, CalculatePi(10000,10000)) ⇒
        }
      }

      // this will run on all nodes
      // use barrier to coordinate test steps
      testConductor.enter("frontend1-started")
    }

    "start two backends which automatically registers, verify service" in within(20 seconds) {
      runOn(backend1) {
        Cluster(system) join node(frontend1).address
        system.actorOf(Props[PiBackend], name = "backend")
      }

      testConductor.enter("backend1-started")

      runOn(backend2) {
        Cluster(system) join node(frontend1).address
        system.actorOf(Props[PiBackend], name = "backend")
      }

      testConductor.enter("backend2-started")

      runOn(frontend1) {
        assertServiceOk()
      }

      testConductor.enter("all-ok")
    }

  }

  def assertServiceOk(): Unit = {
    val piFrontend = system.actorSelection("akka://" + system.name + "/user/frontend")
    val someActor  = system.actorOf(Props(new Actor {
                        def receive = { 
                            case x : CalculatePi ⇒ piFrontend ! x
                            case y : CalculatePiFailed ⇒ println("\n\t No backend is up to service request")
                            case r : ApproximatedPi ⇒ println(s"\n\t YES!!! ${r.pi}"); testActor ! r
                        }}))
    awaitAssert {
      someActor ! CalculatePi(100,100)
      expectMsgType[ApproximatedPi](5.second).pi must equal(3.2454043060553874)
    }
  }

}
