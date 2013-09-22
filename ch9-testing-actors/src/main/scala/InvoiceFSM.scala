import akka.actor._
import collection.mutable.SynchronizedStack

object RestaurantBehavior {
    sealed trait State
    case object Opening extends State
    case object TakingOrders extends State
    case object Idle extends State
    case object Closing extends State

    sealed trait Data
    
}

