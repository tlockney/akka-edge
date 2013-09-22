import akka.actor._
import collection.mutable.SynchronizedStack

sealed trait Message
case class Checkin(amount: Int) extends Message
case class Checkout extends Message

class InvoiceActor extends Actor with ActorLogging {
    private[this] var orders = Order()

    def receive = {
        case Checkin(amount) ⇒ orders.add(amount)
        case Checkout        ⇒ sender ! orders.tally
    }

    def topOfOrder = orders.top
}

trait Order { 
    // Our restaurant is special: we only accept dollars, no coins!
    // we need to synchronized access to our shared structure
    protected[this] var orders : SynchronizedStack[Int] = new SynchronizedStack[Int]()

    def add(amount: Int) : Unit = orders = orders push amount

    def tally : Int = orders.foldLeft(0)(_ + _)

    def apply(x: Int) = orders.applyOrElse(x, (_:Int) match { case _ ⇒ 0 })

    def numOfOrders : Int = orders.size

    def top = orders.top
}

object Order {
    def apply() = new Order {}
}
