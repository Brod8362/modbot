package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class ConversationReplyHandler(prefix: String)(implicit guildDataManager: GuildDataManager, conversationTracker: ConversationTracker) extends ListenerAdapter {
  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    val command = event.getMessage.getContentRaw.matches("\\W.*") //generic matcher for bot commands. not foolproof
    guildDataManager(event.getGuild).logChannel match {
      case Some(channel) if channel == event.getChannel && !event.getAuthor.isBot =>
        guildDataManager(event.getGuild).activeConversation match {
          case Some(conversation) if !command =>
            conversation.sendReply(event.getMessage)
          case Some(_) if command =>
            event.getMessage.reply("> *Bot messages and commands will not be sent to the user.*")
          case None =>
          //no conversation active right now
        }
      case _ =>
      //do nothing
    }
  }
}
