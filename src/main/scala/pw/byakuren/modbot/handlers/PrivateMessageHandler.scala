package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.conversation.{Conversation, ConversationTracker}

class PrivateMessageHandler(implicit tracker: ConversationTracker) extends ListenerAdapter {

  override def onPrivateMessageReceived(event: PrivateMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    tracker(event.getAuthor) match {
      case Some(c) =>
        c.handleMessage(event.getMessage)
      case None =>
        val c = tracker.create(event.getAuthor)
        c.handleMessage(event.getMessage)
    }
  }
}
