package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.{FromConfig}
import akka.remote.RemotingLifecycleEvent

object Bookmarker extends App {

  val system = ActorSystem("bookmarker")

  val database = Database.connect[Bookmark, UUID]("bookmarkDatabase")

  // Launch the `remote` actors and the configuration in the 'deployment'
  // will direct the request back to the listener which is tuning into at 
  // port 2552
  val crawlers = Seq(system.actorOf(Props[Crawler], "crawler-0"), system.actorOf(Props[Crawler], "crawler-1"))

  val bookmarkStoreGuardian =
    system.actorOf(Props(new BookmarkStoreGuardian(database, collection.immutable.Seq("akka.tcp://bookmarker@127.0.0.1:2552/user/crawler-0", "akka.tcp://bookmarker@127.0.0.1:2552/user/crawler-1"))).withDispatcher("boundedBookmarkDispatcher"))

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(system, bookmarkStoreGuardian)), "/")

  val eventStreamListener = system.actorOf(Props(new Actor {
    def receive = {
      case msg ⇒ println(msg)
    }
  }))
  system.eventStream.subscribe(eventStreamListener, classOf[RemotingLifecycleEvent])

  server.setHandler(root)
  server.start
  server.join
}
