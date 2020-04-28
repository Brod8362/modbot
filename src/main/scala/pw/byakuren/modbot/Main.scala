package pw.byakuren.modbot

import java.io.File

import net.dv8tion.jda.api.entities.{Message, User}
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{JDA, JDABuilder}
import pw.byakuren.modbot.cache.ConversationCache
import pw.byakuren.modbot.commands._
import pw.byakuren.modbot.config.BotConfig
import pw.byakuren.modbot.conversation.ConversationTracker
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.handlers.{BotMentionHandler, CommandExecutionHandler, ConversationReplyHandler, PaginatedMessageHandler, PrivateMessageHandler}
import pw.byakuren.modbot.persistence.ConversationWriteThread
import pw.byakuren.modbot.util.TaskScheduler
import pw.byakuren.modbot.util.Utilities._

object Main extends ListenerAdapter {

  implicit val sql: SQLConnection = new SQLConnection
  val config = new BotConfig(new File("config"))
  val prefix: String = config.getString("prefix").get
  var ownerOption: Option[User] = None

  implicit val scheduler: TaskScheduler = new TaskScheduler
  implicit val conversationCache: ConversationCache = new ConversationCache
  implicit val guildDataManager: GuildDataManager = new GuildDataManager
  implicit val conversationTracker: ConversationTracker = new ConversationTracker
  implicit val paginatedMessageHandler: PaginatedMessageHandler = new PaginatedMessageHandler

  val conversationWriteThread: ConversationWriteThread = new ConversationWriteThread
  conversationWriteThread.start()

  val helpCommands = new HelpCommands

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
    new RecallCommands.RecallConversationGuild(),
    new SetPrefix(),
    helpCommands.Guild,
    new SetGame,
    new SetModRole,
    new CacheDebug)
  )

  val privateCommandRegistry = new CommandRegistry[PrivateCommand](Set(
    new RecallCommands.RecallConversationPrivate(),
    helpCommands.Private
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
    ownerOption = Option(jda.retrieveUserById(config.getString("debuguser").getOrElse("0")).complete())
    jda.addEventListener(new CommandExecutionHandler(ownerOption.get, guildCommandRegistry, privateCommandRegistry))
    jda.addEventListener(paginatedMessageHandler)
    jda.addEventListener(new BotMentionHandler)

    /* register the shutdown hook to catch SIGINT and shutdown gracefully */
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      shutdown(jda)
    }))
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
