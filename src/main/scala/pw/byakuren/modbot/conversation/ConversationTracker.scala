package pw.byakuren.modbot.conversation

import net.dv8tion.jda.api.entities.User
import pw.byakuren.modbot.GuildDataManager
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}

import scala.collection.mutable

class ConversationTracker(implicit guildDataManager: GuildDataManager) extends SQLWritable {

  private val ongoing = new mutable.HashMap[Long, Conversation]
  private val completed = new mutable.HashSet[Conversation]()

  def apply(id: Long): Option[Conversation] = {
    val cf = ongoing.get(id)
    for (c <- cf) {
      if (c.getState==ConversationState.Closed) {
        completed.add(c)
        ongoing.remove(id)
        return None
      }
    }
    cf
  }

  def apply(user: User): Option[Conversation] = apply(user.getIdLong)

  def create(user: User): Conversation = {
    val c = new Conversation(user)
    ongoing.put(user.getIdLong, c)
    c
  }

  override def write(SQLConnection: SQLConnection): Boolean = {
    ongoing.values.filter(_.getState==ConversationState.Closed)
    val r = completed.forall(_.write(SQLConnection))
    completed.clear()
    r
  }

  implicit def completeCallback(conversation: Conversation): Unit = {
    ongoing.remove(conversation.user.getIdLong) match {
      case Some(x) =>
        completed.add(x)
      case _ =>
    }
  }
}
