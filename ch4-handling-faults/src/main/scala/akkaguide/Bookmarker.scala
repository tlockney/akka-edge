package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import akka.actor.{Props, ActorSystem}

object Bookmarker extends App {

  val system = ActorSystem("bookmarker")

  val database = Database.connect[Bookmark, UUID]("bookmarkDatabase")

  val bookmarkStoreGuardian =
    system.actorOf(Props(new BookmarkStoreGuardian(database)))

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(system, bookmarkStoreGuardian)), "/")

  server.setHandler(root)
  server.start
  server.join
}
