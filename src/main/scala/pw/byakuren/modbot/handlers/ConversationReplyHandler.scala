package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class ConversationReplyHandler(prefix: String)(implicit guildDataManager: GuildDataManager, conversationTracker: ConversationTracker) extends ListenerAdapter {

  private val ghostEmoji = "\uD83D\uDC7B"

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    val command = event.getMessage.getContentRaw.matches("\\W.*") //generic matcher for bot commands. not foolproof
    guildDataManager(event.getGuild).logChannel match {
      case Some(channel) if channel == event.getChannel =>
        guildDataManager(event.getGuild).activeConversation match {
          case Some(conversation) if event.getMessage.getContentRaw == "end" =>
            conversation.complete()
          case Some(conversation) if command || event.getAuthor.isBot =>
            if (!conversation.commandAlertFlag) {
              event.getMessage.reply(s"> *Bot messages and commands will not be sent to the user. " +
                s"These messages will be denoted with $ghostEmoji.*")
              conversation.commandAlertFlag = true
            }
            event.getMessage.addReaction(ghostEmoji).queue() //ghost emoji
          case Some(conversation) if !command =>
            conversation.sendReply(event.getMessage)
          case None =>
          //no conversation active right now
        }
      case _ =>
      //do nothing
    }
  }
}
