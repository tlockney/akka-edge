package akkaguide

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.annotation.WebServlet
import java.util.UUID
import scala.collection.concurrent.{ Map ⇒ ConcurrentMap }

@WebServlet(name = "bookmarkServlet", urlPatterns = Array("/"))
class BookmarkServlet(bookmarks: ConcurrentMap[UUID, Bookmark]) extends HttpServlet {

  override def doPost(req: HttpServletRequest,
                      res: HttpServletResponse) {

    val out = res.getOutputStream()
    val title = req.getParameter("title")
    val url = req.getParameter("url")
    val bookmark = Bookmark(title, url)
    val uuid = UUID.randomUUID()
    bookmarks.put(uuid, bookmark)

    out.print("Stored bookmark with uuid: " + uuid)
  }

  override def doGet(req: HttpServletRequest,
                     res: HttpServletResponse) {

    val out = res.getOutputStream()
    val bookmarkId = req.getParameter("uuid")
    bookmarks.get(UUID.fromString(bookmarkId)) match {
      case Some(bookmark) ⇒
        out.println("Retrieved " + bookmark)
      case None ⇒
        out.println("Bookmark with UUID specified does not exist.")
    }
  }
}
