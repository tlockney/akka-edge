package akkaguide

import akka.actor.{Props, ActorRef, OneForOneStrategy, Actor}
import akka.actor.SupervisorStrategy.Restart
import scala.concurrent.duration._
import akka.routing.RoundRobinRouter
import java.util.UUID

class BookmarkStoreGuardian(database: Database[Bookmark, UUID]) extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 30 seconds) {
      case _: Exception ⇒ Restart
    }

  val bookmarkStore =
    context.actorOf(Props(new BookmarkStore(database)).
      withRouter(RoundRobinRouter(nrOfInstances = 10)))

  def receive = {
    case msg ⇒ bookmarkStore forward msg
  }
}
