package pw.byakuren.modbot.handlers

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class ConversationReplyHandler(prefix: String)(implicit guildDataManager: GuildDataManager, conversationTracker: ConversationTracker) extends ListenerAdapter {

  private val ghostEmoji = "\uD83D\uDC7B"
  var blacklistGhostIds = Seq(Long)

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    val isCommand = event.getMessage.getContentRaw.matches("[a-zA-Z]?[!$%^&.,>\\[\\]{}|\\\\]([a-zA-Z].*\\s?)*") //generic matcher for bot commands. not foolproof
    guildDataManager(event.getGuild).logChannel match {
      case Some(channel) if channel == event.getChannel =>
        guildDataManager(event.getGuild).activeConversation match {
          case Some(conversation) if event.getMessage.getContentRaw.toLowerCase == "end" =>
            conversation.complete()
          case Some(conversation) if isCommand || event.getAuthor.isBot =>
            if (!conversation.commandAlertFlag) {
              event.getMessage.reply(s"> *Bot messages and commands will not be sent to the user. " +
                s"These messages will be denoted with $ghostEmoji.*")
              conversation.commandAlertFlag = true
            }
            if (conversation.allowGhostOnNextMessageFlag) {
              event.getMessage.addReaction(ghostEmoji).queue() //ghost emoji
            }
            conversation.allowGhostOnNextMessageFlag = true
          case Some(conversation) if !isCommand =>
            conversation.sendReply(event.getMessage)
          case None =>
          //no conversation active right now
        }
      case _ =>
      //do nothing
    }
  }
}
