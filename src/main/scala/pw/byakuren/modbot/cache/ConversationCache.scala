package pw.byakuren.modbot.cache

import java.util.UUID

import pw.byakuren.modbot.conversation.PreviousConversation



class ConversationCache {

  var convs: Seq[PreviousConversation] = Seq()

  def add(previousConversation: PreviousConversation): Unit = {
    convs = convs++Seq(previousConversation)
  }

  def search(uuid: UUID): Seq[PreviousConversation] = search(uuid.toString)


  def search(uuid: String): Seq[PreviousConversation] = {
    convs.filter(_.matchesUuid(uuid))
  }

}
