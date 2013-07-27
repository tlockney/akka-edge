package akkaguide

import java.util.UUID
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.annotation.WebServlet
import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import javax.servlet.AsyncContext

@WebServlet(asyncSupported = true)
class BookmarkServlet(system: ActorSystem, bookmarkStore: ActorRef) extends HttpServlet {

  import BookmarkStore.{AddBookmark, GetBookmark}

  override def doPost(req: HttpServletRequest,
                      res: HttpServletResponse) {
    implicit val ec = ExecutionContext.Implicits.global
    val asyncCtx = req.startAsync()
    val title = req.getParameter("title")
    val url = req.getParameter("url")
    val responder = system.actorOf(Props(classOf[PostResponder], asyncCtx))
    responder ! AddBookmark(title, url)
  }

  override def doGet(req: HttpServletRequest,
                     res: HttpServletResponse) {

    implicit val ec = ExecutionContext.Implicits.global

    val asyncCtx = req.startAsync()
    val writer = asyncCtx.getResponse.getWriter
    val bookmarkId = UUID.fromString(req.getParameter("id"))
    val responder = system.actorOf(Props(classOf[GetResponder], asyncCtx))
    responder ! GetBookmark(bookmarkId)
  }

  override def destroy() {
    system.shutdown()
  }

  class PostResponder(asyncCtx: AsyncContext) extends Actor {
    val writer = asyncCtx.getResponse.getWriter
    def receive = {
      case Some(bookmark) ⇒
        writer.write(bookmark.toString)
      case None ⇒
        writer.write("Could not retrieve bookmark.")
    }
  }

  class GetResponder(asyncCtx: AsyncContext) extends Actor {
    val writer = asyncCtx.getResponse.getWriter
    def receive = {
      case Some(uuid) ⇒
        writer.write("Successfully created bookmark with UUID: " + uuid)
      case None ⇒
        writer.write("Could not create bookmark.")
    }
  }
}
