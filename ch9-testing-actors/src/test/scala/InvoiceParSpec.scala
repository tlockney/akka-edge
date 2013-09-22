import akka.testkit._
import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.{FlatSpecLike, BeforeAndAfterAll, FunSuiteLike, ParallelTestExecution}
import org.scalatest.matchers.MustMatchers

class InvoiceActorFunParSpec extends TestKit(ActorSystem()) with ImplicitSender 
                                                      with FunSuiteLike 
                                                      with BeforeAndAfterAll 
                                                      with MustMatchers
                                                      with ParallelTestExecution {

    test("have three values {400, 500, 600} when the same three orders are issued") {
        val actor = TestActorRef[InvoiceActor]
        val ref   = actor.underlyingActor
        actor ! Checkin(400)
        ref.topOfOrder must be (400)
        actor ! Checkin(500)
        ref.topOfOrder must be (500) 
        actor ! Checkin(600)
        ref.topOfOrder must be (600)
    }

    test("return a tally of 1,500 after three orders of value 400, 500 & 600") {
        val actor = TestActorRef[InvoiceActor]
        actor ! Checkin(400)
        actor ! Checkin(500)
        actor ! Checkin(600)
        actor ! Checkout
        expectMsg(1500)
    }

    override def afterAll() = { system.shutdown}
}

class InvoiceActorFunParSpec2 extends TestKit(ActorSystem()) with ImplicitSender 
                                                      with FunSuiteLike 
                                                      with BeforeAndAfterAll 
                                                      with MustMatchers
                                                      with ParallelTestExecution {

    test("have one value {400} when one order is issued") {
        val actor = TestActorRef[InvoiceActor]
        val ref   = actor.underlyingActor
        actor ! Checkin(400)
        ref.topOfOrder must be (400) 
    }

    test("return a tally of 500 after one order of value 500") {
        val actor = TestActorRef[InvoiceActor]
        actor ! Checkin(500)
        actor ! Checkout
        expectMsg(500)
    }

    override def afterAll() = { system.shutdown}
}
