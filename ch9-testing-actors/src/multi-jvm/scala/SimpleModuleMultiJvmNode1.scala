import org.scalatest._
import org.scalatest.matchers.MustMatchers._
import akka.actor._
import akka.testkit._

object SimpleModuleMultiJvmNode1 extends TestKit(ActorSystem()) with Configuration with ImplicitSender
//object SimpleModuleMultiJvmNode1 extends Configuration with TestKitBase 
                                                       with FlatSpecLike 
                                                       with MustMatchers 
                                                       with BeforeAndAfterAll {
    //implicit lazy val system = ActorSystem("SimpleModuleMultiJvmNode1")
    //implicit def self = testActor

    def doAction(f: ⇒ Unit) = f

    def main(args: Array[String]) : Unit = {
        try doAction{ 
	        val p = getCustomMessageForNode
	        val pn = getThreadPoolName
	        pn must include ("ForkJoinPool")
            val invoice = TestActorRef[InvoiceActor]
            invoice ! Checkin(500)
            invoice ! Checkout
            expectMsg(500)
        } finally afterAll 
    }

    override def afterAll = { system.shutdown }
}

