package pw.byakuren.modbot.commands
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.cache.ConversationCache
import pw.byakuren.modbot.util.Utilities._

class CacheDebug(implicit conversationCache: ConversationCache) extends GuildCommand(Seq("cdbg"),"","",CommandPermission.Debug){
  override def run(message: Message, args: Seq[String]): Unit = {
    val convs = conversationCache.convs
    val str = convs.map(_.uuid.toString.substring(0,8)).mkString(",")
    message.reply(f"Cache Size:${convs.size}\n`$str`")
  }
}
