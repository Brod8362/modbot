package pw.byakuren.modbot

import java.io.File
import java.util.EventListener

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.config.BotConfig
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.handlers.PrivateMessageHandler

object Main extends ListenerAdapter {

  val config = new BotConfig(new File("config"))

  def main(args: Array[String]): Unit = {

    implicit val conversationTracker = new ConversationTracker

    val jda = config.getString("token") match {
      case Some(token) =>
        new JDABuilder(token).addEventListeners(
          this,
          new PrivateMessageHandler
        ).build()
      case None =>
        throw new RuntimeException("Token not found, check config file")
    }
  }

}
