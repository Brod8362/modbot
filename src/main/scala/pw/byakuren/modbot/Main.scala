package pw.byakuren.modbot

import java.io.File

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.{JDA, JDABuilder}
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.modbot.commands.{Command, CommandPermission, SetLogChannel, ViewChatQueue}
import pw.byakuren.modbot.config.BotConfig
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}
import pw.byakuren.modbot.handlers.PrivateMessageHandler
import pw.byakuren.modbot.util.Utilities._

object Main extends ListenerAdapter {

  implicit val sql: SQLConnection = new SQLConnection
  val config = new BotConfig(new File("config"))
  val prefix: String = config.getString("prefix").get

  implicit val guildDataManager: GuildDataManager = new GuildDataManager
  implicit val conversationTracker: ConversationTracker = new ConversationTracker

  private val stopCommand = new Command(Seq("stop"), "Stop the bot", CommandPermission.Debug) {
    override def run(message: Message, args: Seq[String]): Unit = {
      message.reply("Shutting down")
      shutdown(message.getJDA)
    }
  }

  val commandRegistry = new CommandRegistry(Set(new SetLogChannel(),
    new ViewChatQueue(),
    stopCommand)
  )

  val sqlWritable: Seq[SQLWritable] = Seq(guildDataManager)

  def main(args: Array[String]): Unit = {
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

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    if (event.getAuthor.isBot) return
    if (event.getMessage.getContentRaw.startsWith(prefix)) {
      val args = event.getMessage.getContentRaw.substring(prefix.length).split(" ").toSeq
      commandRegistry(args.head) match {
        case Some(command) =>
          //TODO check permission
          command.run(event.getMessage, args drop 1)
        case _ =>
          event.getMessage.reply("Command not found")
      }
    }
  }

  override def onReady(event: ReadyEvent): Unit = {
    guildDataManager.loadGuilds(event.getJDA, sql)
  }

  def shutdown(jda: JDA): Unit = {
    sqlWritable.forall(_.write(sql))
    jda.shutdown()
  }
}
