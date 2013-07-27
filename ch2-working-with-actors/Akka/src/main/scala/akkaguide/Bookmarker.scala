package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import akka.actor.{OneForOneStrategy, Props, ActorSystem}
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import scala.concurrent.duration._
import akka.routing.RoundRobinRouter

object Bookmarker extends App {

  val system = ActorSystem("bookmarker")

  val database = Database.connect[Bookmark, UUID]("bookmarkDatabase")

  val databaseSupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 30 seconds) {
    case e: Exception ⇒ Restart
    case _ ⇒ Escalate
  }

  val bookmarkStore =
    system.actorOf(Props(classOf[BookmarkStore], database).
      withRouter(RoundRobinRouter(nrOfInstances = 10, supervisorStrategy = databaseSupervisorStrategy)))

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(system, bookmarkStore)), "/")

  server.setHandler(root)
  server.start
  server.join
}
