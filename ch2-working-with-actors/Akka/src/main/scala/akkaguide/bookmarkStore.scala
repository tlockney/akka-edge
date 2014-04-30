package akkaguide

import akka.actor.Actor
import java.util.UUID

object BookmarkStore {
  case class AddBookmark(title: String, url: String)
  case class GetBookmark(uuid: UUID)
}

class BookmarkStore(database: Database[Bookmark, UUID]) extends Actor {
  import BookmarkStore.{GetBookmark, AddBookmark}
  def receive = {
    case AddBookmark(title, url) ⇒
      val bookmark = Bookmark(title, url)
      database.find(bookmark) match {
        case Some(found) ⇒ sender ! None
        case None ⇒
          val newUUID = UUID.randomUUID
          database.create(newUUID, bookmark)
          sender ! Some(newUUID)
      }
    case GetBookmark(uuid) ⇒
      sender ! database.read(uuid)
  }
}
