package pw.byakuren.modbot

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.commands.Command
import pw.byakuren.modbot.util.Utilities._

import scala.collection.mutable

class CommandExecutor {

  private val storedErrors = new mutable.ListBuffer[(String, Exception)]

  def execute(command: Command, message: Message, args: Seq[String]): Unit = {
    new Thread(() => {
      try {
        command.run(message, args)
      } catch {
        case e:Exception => handleException(message, e, message.getContentRaw)
      }
    }).start()
  }

  def handleException(message: Message, exception: Exception, invocation: String): Unit = {
    val errorMsg = f"__Uncaught Exception: ${exception.getMessage}__\n```${exception.getStackTrace.mkString("\n")}```"
    storedErrors.addOne((invocation, exception))
    message.reply(errorMsg)
  }

  def getStoredErrors: Seq[(String, Exception)] = storedErrors.toSeq


}
