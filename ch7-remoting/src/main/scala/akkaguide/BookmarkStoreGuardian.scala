package akkaguide

import akka.actor.{Props, OneForOneStrategy, Actor}
import akka.actor.SupervisorStrategy.Restart
import scala.concurrent.duration._
import java.util.UUID
import akka.routing.RoundRobinRouter

class BookmarkStoreGuardian(database: Database[Bookmark, UUID], crawlerNodes: collection.immutable.Seq[String]) extends Actor {

  import akkaguide.BookmarkStore.{GetBookmark, AddBookmark}

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 30 seconds) {
      case _: Exception ⇒ Restart
    }

  val bookmarkStore =
    context.actorOf(Props(new BookmarkStore(database, crawlerNodes)).withRouter(RoundRobinRouter(nrOfInstances = 10)), "bookmarkStore")

  def receive = {
    case a: AddBookmark ⇒ bookmarkStore forward a
    case g: GetBookmark ⇒ bookmarkStore forward g
  }
}
