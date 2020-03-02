package pw.byakuren.modbot

import java.io.File

import net.dv8tion.jda.api.entities.{Message, User}
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{JDA, JDABuilder}
import pw.byakuren.modbot.commands._
import pw.byakuren.modbot.config.BotConfig
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.handlers.{CommandExecutionHandler, ConversationReplyHandler, PrivateMessageHandler}
import pw.byakuren.modbot.util.Utilities._

object Main extends ListenerAdapter {

  implicit val sql: SQLConnection = new SQLConnection
  val config = new BotConfig(new File("config"))
  val prefix: String = config.getString("prefix").get
  var ownerOption: Option[User] = None

  implicit val guildDataManager: GuildDataManager = new GuildDataManager
  implicit val conversationTracker: ConversationTracker = new ConversationTracker

  private val stopCommand = new GuildCommand(Seq("stop"), "Stop the bot", "", CommandPermission.Debug) {
    override def run(message: Message, args: Seq[String]): Unit = {
      message.reply("Shutting down")
      shutdown(message.getJDA)
    }
  }

  val guildCommandRegistry = new CommandRegistry[GuildCommand](Set(new SetLogChannel(),
    new ViewChatQueue(),
    stopCommand,
    new ConversationList(),
    new SetConfig(),
    new RecallConversationGuild(),
    new SetPrefix())
  )

  val privateCommandRegistry = new CommandRegistry[PrivateCommand](Set(
    new RecallConversationPrivate()
  ))

  val sqlWritable: Seq[SQLWritable] = Seq(guildDataManager, conversationTracker)

  def main(args: Array[String]): Unit = {
    val jda = config.getString("token") match {
      case Some(token) =>
        new JDABuilder(token).addEventListeners(
          this,
          new PrivateMessageHandler,
          new ConversationReplyHandler(prefix)
        ).build()
      case None =>
        throw new RuntimeException("Token not found, check config file")
    }
    ownerOption = Some(jda.retrieveApplicationInfo().complete().getOwner)
    jda.addEventListener(new CommandExecutionHandler(ownerOption.get, guildCommandRegistry, privateCommandRegistry))
  }

  override def onReady(event: ReadyEvent): Unit = {
    guildDataManager.loadGuilds(event.getJDA)
  }

  def shutdown(jda: JDA): Unit = {
    for (writable <- sqlWritable) {
      try {
        writable.write(sql)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    jda.shutdown()
  }
}
