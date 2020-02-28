package pw.byakuren.modbot.conversation

import net.dv8tion.jda.api.entities.{Guild, Message, User}
import pw.byakuren.modbot.GuildDataManager

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import pw.byakuren.modbot.util.Utilities._

class Conversation(val user: User)(implicit guildDataManager: GuildDataManager) {

  private var state = ConversationState.Init
  private val messages = new mutable.ListBuffer[Message]
  private val sharedGuilds = user.getMutualGuilds.asScala
  private var guildOption: Option[Guild] = None

  def getState: ConversationState.Value = state

  private def addMessage(message: Message): Unit = if (state != ConversationState.Closed) messages.addOne(message)

  if (sharedGuilds.size == 1) {
    setGuild(sharedGuilds.head)
  }

  def handleMessage(message: Message): Unit = {
    if (message.getContentRaw=="end") {
      complete()
      return
    }
    state match {
      case ConversationState.Init =>
        state = ConversationState.ServerInit
        message.reply(f"You are in multiple servers that use this bot. Please pick which server you would like" +
          s" to contact.\n ${makeServerListString(sharedGuilds.toSeq)}")
      case ConversationState.ServerInit =>
        //Waiting for a reply that's a number.
        message.getContentRaw.toIntOption match {
          case Some(i) if sharedGuilds.indices contains i - 1 =>
            setGuild(sharedGuilds(i - 1))
          case _ =>
            message.reply(f"Please reply with a number 1 to ${sharedGuilds.size}")
        }
      case _ => {
        addMessage(message)
        guildDataManager(guildOption.get).logChannel match {
          case Some(channel) =>
            channel.sendMessage(message).queue()
          case _ =>
        }
      }
    }
  }

  def setGuild(newGuild: Guild): Unit = {
    guildOption = Some(newGuild)
    user.openPrivateChannel().complete().sendMessage(
      f"You are now messaging the moderators of ${newGuild.getName} directly. " +
        f"A log of this conversation will be recorded and sent to the moderators. The moderators can reply anonymously " +
        f"through this bot, but may or may not choose to identify themselves. To end the conversation, say `end`").queue()
    state = ConversationState.InProgress
  }

  //todo handle deleted and edited messages. may require restructuring of class

  /**
   * End the conversation, preventing any new messages. Any further messages will be considered a new conversation.
   *
   * @return A List[Message] of all the messages, in order.
   */
  def complete(): List[Message] = {
    state = ConversationState.Closed
    user.openPrivateChannel().complete().sendMessage(
      f"The conversation has conlcuded and ${messages.size} messages have been recorded. You may access the log " +
        f"again at anytime by running the command (TBD)"
    ).queue()
    messages.result()
  }

  private def makeServerListString(servers: Seq[Guild]): String = {
    val a = for (i <- servers.indices) yield {
      f"${i+1}: ${servers(i).getName}"
    }
    a.mkString("\n")
  }
}
