package akkaguide

import java.util.UUID
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.annotation.WebServlet
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@WebServlet(asyncSupported = true)
class BookmarkServlet(system: ActorSystem, bookmarkStore: ActorRef) extends HttpServlet {

  import BookmarkStore.{AddBookmark, GetBookmark}

  override def doPost(req: HttpServletRequest,
                      res: HttpServletResponse) {
    implicit val ec = ExecutionContext.Implicits.global
    val asyncCtx = req.startAsync()
    val writer = asyncCtx.getResponse.getWriter
    val title = req.getParameter("title")
    val url = req.getParameter("url")
    implicit val timeout = Timeout(5 seconds)
    asyncCtx.setTimeout(5 * 1000)
    val uuidFuture = bookmarkStore ? AddBookmark(title, url)
    uuidFuture.mapTo[Option[UUID]].onComplete {
      case Success(uuid) ⇒
        writer.write(s"Successfully created bookmark with uuid=$uuid")
      case Failure(error) ⇒
        writer.write("Failure creating bookmark: " + error.getMessage)
    }
  }

  override def doGet(req: HttpServletRequest,
                     res: HttpServletResponse) {

    implicit val ec = ExecutionContext.Implicits.global

    val asyncCtx = req.startAsync()
    val writer = asyncCtx.getResponse.getWriter
    val bookmarkId = UUID.fromString(req.getParameter("uuid"))

    implicit val timeout = Timeout(5 seconds)
    asyncCtx.setTimeout(5 * 1000)
    val bookmarkFuture = bookmarkStore ? GetBookmark(bookmarkId)

    bookmarkFuture.mapTo[Option[Bookmark]].onComplete {
      case Success(bm) ⇒
        writer.write(bm.getOrElse("Not found").toString)
      case Failure(error) ⇒
        writer.write("Could not retrieve bookmark: " + error.getMessage)
    }
  }

  override def destroy() {
    system.shutdown()
  }
}
