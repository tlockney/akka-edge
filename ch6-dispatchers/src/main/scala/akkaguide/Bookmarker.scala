package akkaguide

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util.UUID
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.routing.{FromConfig}
import scala.util.{Try,Success,Failure}

object Bookmarker extends App {

  val helpMessage =
"""
$ > sbt "run [dispatchername|actorname|mailboxname]"
"""
  val system = ActorSystem("bookmarker")

  val database = Database.connect[Bookmark, UUID]("bookmarkDatabase")

  def startByDispatcherName() : ActorRef = 
    system.actorOf(Props(new BookmarkStoreGuardian(database))
      .withDispatcher("boundedBookmarkDispatcher"))

  def startByActorName() : ActorRef = 
    system.actorOf(Props(new BookmarkStoreGuardian(database)) ,"boundedMailboxactor")

  def startByMailboxName() : ActorRef = 
    system.actorOf(Props(new BookmarkStoreGuardian(database)).withMailbox("boundedBookmarkMailbox"))

  val bookmarkStoreGuardian:ActorRef = Try(args(0)) match {
			                                case Success("dispatchername") ⇒ startByDispatcherName
			                                case Success("actorname")      ⇒ startByActorName
			                                case Success("mailboxname")    ⇒ startByMailboxName
                                            case Success(_)                ⇒ Actor.noSender
			                                case Failure(e) ⇒ Console.println(helpMessage)
			                                                  Actor.noSender
                                        }
                                                    
  assert(bookmarkStoreGuardian != Actor.noSender)

  val server = new Server(8080)
  val root = new ServletContextHandler(ServletContextHandler.SESSIONS)

  root.addServlet(new ServletHolder(new BookmarkServlet(system, bookmarkStoreGuardian)), "/")

  server.setHandler(root)
  server.start
  server.join
}
