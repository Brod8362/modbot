package pw.byakuren.modbot.conversation

import net.dv8tion.jda.api.entities.{Guild, User}

import scala.collection.mutable

class ConversationTracker() {

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

}
