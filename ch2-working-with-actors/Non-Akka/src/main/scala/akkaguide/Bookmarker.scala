package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import scala.collection.concurrent.TrieMap

object Bookmarker extends App {

  val bookmarks = TrieMap[UUID, Bookmark]()

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(bookmarks)), "/")

  server.setHandler(root)
  server.start
  server.join
}
