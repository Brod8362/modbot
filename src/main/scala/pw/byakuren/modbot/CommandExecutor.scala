package pw.byakuren.modbot

import java.io.FileOutputStream
import java.time.LocalDateTime

import net.dv8tion.jda.api.entities.{Message, MessageChannel}
import pw.byakuren.modbot.commands.Command
import pw.byakuren.modbot.util.Utilities._

import scala.collection.mutable
import java.io.File

import pw.byakuren.modbot.guild.GuildDataManager

class CommandExecutor(logLocation: MessageChannel)(implicit guildDataManager: GuildDataManager) {

  private val storedErrors = new mutable.ListBuffer[(String, Exception)]

  if (!new File("log").exists()) {
    new File("log").mkdir()
  }

  def execute(command: Command, message: Message, args: Seq[String]): Unit = {
    new Thread(() => {
      try {
        command.run(message, args)
      } catch {
        case e:Exception => handleException(message, command, e, message.getContentRaw)
      }
    }).start()
  }

  def handleException(message: Message, command: Command, exception: Exception, invocation: String): Unit = {
    val errorMsg = f"__Uncaught Exception:__\n${exception}\n`$invocation`\n```${exception.getStackTrace.mkString("\n")}```"
    val errorData =
      f"""|Exception while running command: $exception
         |Invocation: $invocation
         |User: ${message.getAuthor}
         |Guild: ${Option(message.getGuild)}
         |Guild Config: ${Option(message.getGuild.getSettings.value)}
         |Time: ${LocalDateTime.now}
         |==========
         |${exception.getStackTrace.mkString("\n")}
         |""".stripMargin
    storedErrors.addOne((invocation, exception))
    message.reply("An error has occurred while running your command.")
    val filename=s"strace-${command.name}_${exception.hashCode().toHexString}.txt"
    logLocation.sendMessage(errorMsg).addFile(errorData.getBytes(), filename).queue()
    val f = new FileOutputStream(new File(s"log/$filename"))
    f.write(errorData.getBytes)
    f.close()
  }

  def getStoredErrors: Seq[(String, Exception)] = storedErrors.toSeq

}
