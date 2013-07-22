package akkaguide

import akka.actor.Actor
import io.Source

object Crawler {
  case class RetrievePage(bookmark: Bookmark)
  case class Page(bookmark: Bookmark, contents: String)
}

class Crawler extends Actor {

  import Crawler.{RetrievePage, Page}

  def receive = {
    case RetrievePage(bookmark) ⇒
      val contents = Source.fromURL(bookmark.url).getLines().mkString("\n")
      sender ! Page(bookmark, contents)
  }
}
