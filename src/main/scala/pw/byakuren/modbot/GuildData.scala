package pw.byakuren.modbot

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.{Guild, Member, MessageChannel, Role, TextChannel}
import pw.byakuren.modbot.conversation.Conversation
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}

import scala.collection.mutable

class GuildData(val server: Guild, var logChannel: Option[TextChannel], var moderatorRole: Option[Role] = None)
  extends SQLWritable {

  private val conversationQueue = new mutable.Queue[Conversation]

  def isAdmin(member: Member): Boolean = {
    for (role <- moderatorRole)
      if (member.getRoles.contains(role))
        return true
    member.hasPermission(Permission.ADMINISTRATOR)
  }

  def activeConversation: Option[Conversation] = conversationQueue.headOption

  def addToQueue(conversation: Conversation): Int = {
    conversationQueue.addOne(conversation)
    findPositionOf(conversation)
  }

  def findPositionOf(conversation: Conversation): Int = {
    conversationQueue.indexOf(conversation)
  }

  def nextConversationInQueue(): Option[Conversation] = {
    conversationQueue.removeHead(true)
    for (c <- 1 until conversationQueue.size) {
      alertQueuePosition(conversationQueue(c))
    }
    conversationQueue.headOption
  }

  def queueSize: Int = conversationQueue.size

  def alertQueuePosition(conversation: Conversation): Unit = {
    conversation.alertPosition(findPositionOf(conversation))
  }

  def getQueue(): Seq[Conversation] = {
    conversationQueue.clone().toSeq
  }

  override def write(SQLConnection: SQLConnection): Boolean = {
    logChannel.map(SQLConnection.setGuildLogChannel).get
  }
}
