package pw.byakuren.modbot.util

import net.dv8tion.jda.api.entities.Message

object Utilities {
  implicit class MessageUtils(message: Message) {
    def reply(replyText: String): Unit = {
      message.getChannel.sendMessage(replyText).queue()
    }
  }
}
