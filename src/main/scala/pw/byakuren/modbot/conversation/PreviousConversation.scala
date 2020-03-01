package pw.byakuren.modbot.conversation

import java.util.UUID

import net.dv8tion.jda.api.entities.{Guild, User}

class PreviousConversation(val uuid: UUID, val user: User, val guild: Guild, messages: Seq[(User, String)]) {

  def privateRepresentation: String = {
    s"`Conversation: $uuid`\n"+messages.map(t => f"${if (t._1==user) t._1.getAsMention else "<=="}: ${t._2}").mkString("\n")
  }

  def guildRepresentation: String = {
    s"`Conversation: $uuid`\n"+messages.map(t => f"${t._1.getAsMention}: ${t._2}").mkString("\n")
  }
}
