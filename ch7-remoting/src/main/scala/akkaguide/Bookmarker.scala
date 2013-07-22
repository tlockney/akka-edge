package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.{FromConfig}
import akka.remote.RemoteLifeCycleEvent

object Bookmarker extends App {

  val system = ActorSystem("bookmarker")

  val database = Database.connect[Bookmark, UUID]("bookmarkDatabase")

  val bookmarkStoreGuardian =
    system.actorOf(Props(new BookmarkStoreGuardian(database, Seq[String]()))
      .withDispatcher("boundedBookmarkDispatcher"))

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(system, bookmarkStoreGuardian)), "/")

  val eventStreamListener = system.actorOf(Props(new Actor {
    def receive = {
      case msg ⇒ println(msg)
    }
  }))
  system.eventStream.subscribe(eventStreamListener, classOf[RemoteLifeCycleEvent])

  server.setHandler(root)
  server.start
  server.join
}
