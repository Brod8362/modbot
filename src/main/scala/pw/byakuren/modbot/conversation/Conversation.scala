package pw.byakuren.modbot.conversation

import java.sql.Time
import java.time.LocalTime
import java.util.UUID

import net.dv8tion.jda.api.entities.{Guild, Message, User}
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}
import pw.byakuren.modbot.guild.GuildDataManager

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import pw.byakuren.modbot.util.Utilities._

class Conversation(val user: User)(implicit guildDataManager: GuildDataManager, completeCallback: Conversation => Unit)  extends SQLWritable {

  private var state = ConversationState.Init
  private val messages = new mutable.ListBuffer[Message]
  private val sharedGuilds = user.getMutualGuilds.asScala
  private var guildOption: Option[Guild] = None
  val uuid = UUID.randomUUID()
  val time = Time.valueOf(LocalTime.now())
  var commandAlertFlag = false //this is set true on the first instance of a command being run on the moderator's side.

  def getState: ConversationState.Value = state

  private def addMessage(message: Message): Unit = if (state != ConversationState.Closed) messages.addOne(message)

  if (sharedGuilds.size == 1) {
    setGuild(sharedGuilds.head)
  }

  def handleMessage(message: Message): Unit = {
    if (message.getContentRaw == "end") {
      complete()
      return
    }
    state match {
      case ConversationState.Init =>
        state = ConversationState.ServerInit
        message.reply(f"You are in multiple servers that use this bot. Please pick which server you would like" +
          s" to contact.\n ${makeServerListString(sharedGuilds.filter(_.logChannel.isDefined).toSeq)}")
      case ConversationState.ServerInit =>
        //Waiting for a reply that's a number.
        message.getContentRaw.toIntOption match {
          case Some(i) if sharedGuilds.indices contains i - 1 =>
            setGuild(sharedGuilds(i - 1))
          case _ =>
            message.reply(f"Please reply with a number 1 to ${sharedGuilds.size}")
        }
      case ConversationState.Waiting =>
        message.reply("You're currently in the queue. Please wait. Anything said now is not logged, and the moderators will not be able" +
          " to see it.")
      case _ =>
        addMessage(message)
        if (!message.getAuthor.equals(message.getJDA.getSelfUser))
          sendGuildMessage(f"[${message.getAuthor.getAsMention}] ${message.getContentRaw}")
    }
  }

  def setGuild(newGuild: Guild): Unit = {
    guildOption = Some(newGuild)
    state = ConversationState.Waiting
    val pos = newGuild.getData.addToQueue(this)
    if (pos == 0) {
      start()
    } else {
      sendGuildMessage(f"`==>` ${user.getAsMention} `has entered the queue.`")
      user.openPrivateChannel().complete()
        .sendMessage("There are other people talking to the moderators right now, you will be put into the queue.")
        .queue()
      alertPosition(pos)
    }
  }

  def start(): Unit = {
    val g = guildOption.get
    user.openPrivateChannel().complete().sendMessage(
      f"You are now messaging the moderators of ${g.getName} directly. " +
        f"A log of this conversation will be recorded and sent to the moderators. The moderators can reply anonymously " +
        f"through this bot, but may or may not choose to identify themselves. To end the conversation, say `end`").queue()
    sendGuildMessage(f"`==>` ${user.getAsMention} `has entered the chat.`")
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
    sendGuildMessage(f"`==>` ${user.getAsMention} `'s chat has ended.`\n`ID:${uuid.toString.substring(0,8)}\nMessages:${messageLog.size}`")
    for (conversation <- guildOption.get.getData.nextConversationInQueue()) {
      conversation.start()
    }
    completeCallback(this)
    messages.result()
  }

  private def makeServerListString(servers: Seq[Guild]): String = {
    val a = for (i <- servers.indices) yield {
      f"${i + 1}: ${servers(i).getName}"
    }
    a.mkString("\n")
  }

  /**
   * Send an alert to a waiting conversation about it's new position.
   *
   * @param pos The position of the conversation, with 1 being next in line.
   */
  def alertPosition(pos: Int): Unit = {
    user.openPrivateChannel().complete().sendMessage(f"You are now ${pos.toPositionString} in line.").queue()
  }

  private def sendGuildMessage(string: String): Unit = {
    for (guild <- guildOption; logChannel <- guild.getData.logChannel)
      logChannel.sendMessage(string).queue()
  }

  def sendReply(message: Message): Unit = {
    user.openPrivateChannel().complete().sendMessage(f"> ${message.getContentRaw}").queue()
    messages.addOne(message)
  }

  def messageLog: Seq[Message] = messages.toSeq

  override def write(SQLConnection: SQLConnection): Unit = {
    SQLConnection.writeConversation(this) > 0
  }

  def getGuild: Option[Guild] = guildOption
}
