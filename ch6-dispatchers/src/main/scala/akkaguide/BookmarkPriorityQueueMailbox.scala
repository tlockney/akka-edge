package akkaguide

import akka.dispatch.{BoundedPriorityMailbox, PriorityGenerator, UnboundedPriorityMailbox}
import akka.actor.ActorSystem
import com.typesafe.config.Config

object BookmarkPriorityQueueMailbox {
  import akkaguide.BookmarkStore.AddBookmark

  val priorityGenerator = PriorityGenerator {
    case AddBookmark(title, url) if url.contains("typesafe.com") ⇒ 0
    case AddBookmark(title, url) if url.contains("oracle.com") ⇒ 2
    case _ ⇒ 1
  }
}

class UnboundedBookmarkPriorityQueueMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(BookmarkPriorityQueueMailbox.priorityGenerator)
