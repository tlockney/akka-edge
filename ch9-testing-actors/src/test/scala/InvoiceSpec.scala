import akka.testkit._
import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.{FlatSpecLike, BeforeAndAfterAll, FunSuiteLike, ParallelTestExecution}
import org.scalatest.matchers.MustMatchers

class InvoiceActorSpec extends TestKit(ActorSystem()) with ImplicitSender 
                                                      with FlatSpecLike 
                                                      with BeforeAndAfterAll 
                                                      with MustMatchers {
    behavior of "`Invoice` actor taking 2 orders" 

    it should "have two values {400, 500} when the same two orders are issued" in {
        val actor = TestActorRef[InvoiceActor]
        val ref   = actor.underlyingActor
        actor ! Checkin(400)
        ref.topOfOrder must be (400)
        actor ! Checkin(500)
        ref.topOfOrder must be (500) 
    }

    it should "return a tally of 900 after two orders of value 400 & 500" in {
        val actor = TestActorRef[InvoiceActor]
        actor ! Checkin(400)
        actor ! Checkin(500)
        actor ! Checkout
        expectMsg(900)
    }

    override def afterAll() = { system.shutdown}
}

