import akka.testkit._
import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.{FlatSpecLike, BeforeAndAfterAll, FunSuiteLike, ParallelTestExecution}
import org.scalatest.matchers.MustMatchers
import scala.concurrent.duration._

class InvoiceActorTimeSpec extends TestKit(ActorSystem()) with ImplicitSender 
                                                      with FlatSpecLike 
                                                      with BeforeAndAfterAll 
                                                      with MustMatchers
                                                      with ParallelTestExecution {
    behavior of "`Invoice` actor taking 2 orders must complete within 800 millis" 

    it should "have two values {400, 500} when the same two orders are issued" in {
        val actor = TestActorRef[InvoiceActor]
        val ref   = actor.underlyingActor
        within (800 millis) {
            within(500 millis) {
                actor ! Checkin(400)
                ref.topOfOrder must be (400)
            }
            within(500 millis) {
                actor ! Checkin(500)
                ref.topOfOrder must be (500) 
            }
        }
    }

    it should "return a tally of 900 after two orders of value 400 & 500" in {
        val actor = TestActorRef[InvoiceActor]
        within (800 millis) {
            actor ! Checkin(400)
            actor ! Checkin(500)
            actor ! Checkout
            expectMsg(900)
        }
    }

    override def afterAll() = { system.shutdown}
}

