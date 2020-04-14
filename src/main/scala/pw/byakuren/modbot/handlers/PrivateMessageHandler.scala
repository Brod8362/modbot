package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.Main
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class PrivateMessageHandler(implicit tracker: ConversationTracker, guildDataManager: GuildDataManager) extends ListenerAdapter {

  override def onPrivateMessageReceived(event: PrivateMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    tracker(event.getAuthor) match {
      case Some(c) =>
        c.handleMessage(event.getMessage)
      case None =>
        if (event.getMessage.getContentRaw.toLowerCase=="end") {
          event.getMessage.reply("No ongoing conversation to end.")
        } else if (!event.getMessage.getContentRaw.startsWith(Main.prefix)) {
          val c = tracker.create(event.getAuthor)
          c.handleMessage(event.getMessage)
        }
    }
  }
}
