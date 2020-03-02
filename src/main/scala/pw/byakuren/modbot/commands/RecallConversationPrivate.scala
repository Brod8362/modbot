package pw.byakuren.modbot.commands
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.database.SQLConnection
import pw.byakuren.modbot.util.Utilities._

class RecallConversationPrivate(implicit SQLConnection: SQLConnection) extends PrivateCommand(Seq("recall"), "","") {
  override def run(message: Message, args: Seq[String]): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a UUID!")
      return
    }
    val c = SQLConnection.getConversations(args.head, message.getAuthor)
    c.size match {
      case 1 =>
        message.reply(c.head.privateRepresentation)
      case 0 =>
        message.reply(s"No conversation with ID ${args.head} found.")
      case _ =>
        message.reply(s"More than one conversation matches that ID. Try being more specific.")
    }
  }
}
