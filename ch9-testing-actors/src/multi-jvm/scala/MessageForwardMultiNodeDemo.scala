import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import akka.testkit.ImplicitSender
import akka.remote.testkit._

trait ForwardMultiNodeSpec extends MultiNodeSpecCallbacks
                    with WordSpecLike
                    with MustMatchers 
                    with BeforeAndAfterAll {

    override def beforeAll() = multiNodeSpecBeforeAll()

    override def afterAll() = multiNodeSpecAfterAll()
}

object MConfig extends MultiNodeConfig {
    val node1 = role("Actor-1")
    val node2 = role("Actor-2")
    val node3 = role("Actor-3")
}

class ForwardMNMultiJvmNode1 extends ForwardMultipleNodeDemo
class ForwardMNMultiJvmNode2 extends ForwardMultipleNodeDemo
class ForwardMNMultiJvmNode3 extends ForwardMultipleNodeDemo
case class Stop

class ForwardMultipleNodeDemo extends MultiNodeSpec(MConfig) 
                       with ForwardMultiNodeSpec 
                       with ImplicitSender {

    import MConfig._

    def initialParticipants = roles.size

    override def verifySystemShutdown = true

    override def atStartup() = println("STARTING UP!")
    override def afterTermination() = println("TERMINATION!")

    "A multi-node illustrating `message-forwarding`" must {

        "wait for all nodes to enter barrier" in {

            enterBarrier("startingup")
    
        }

        "send to and receive from a remote node via `forwarding`" in {

            runOn(node1) {
                enterBarrier("deployed")
                var condition = false
                val next = system.actorFor(node(node2) / "user" / "MiddleMan")
                val me = system.actorOf(Props(new Actor with ActorLogging {
                    def receive = {
                        case "hello" ⇒ next forward (self, "hello")
                        case ("hello",Stop)  ⇒ condition = true
                    }}), "Initiator")

                me ! "hello"
                awaitCond(condition == true)
            }

            runOn(node2) {
                val next = system.actorFor(node(node3) / "user" / "Destinator")
                system.actorOf(Props(new Actor with ActorLogging {
                    def receive = {
                        case (to, msg) ⇒ next forward (to, msg)
                    }}), "MiddleMan")
                enterBarrier("deployed")
            }

            runOn(node3) {
                system.actorOf(Props(new Actor with ActorLogging {
                    def receive = {
                        case (to:ActorRef, msg) ⇒ to ! (msg, Stop)
                    }}), "Destinator")
                enterBarrier("deployed")
            }
        
        enterBarrier("finished")
    }
}
}

