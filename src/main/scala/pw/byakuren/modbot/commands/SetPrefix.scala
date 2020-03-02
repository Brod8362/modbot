package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.{Guild, Message}
import pw.byakuren.modbot.guild.{GuildDataManager, GuildSetting}
import pw.byakuren.modbot.util.Utilities._

import scala.collection.mutable

class SetPrefix(implicit guildDataManager: GuildDataManager) extends
  GuildCommand(Seq("prefix"), "", "", CommandPermission.Admins) {
  val pending = new mutable.HashMap[Guild, String]
  override def run(message: Message, args: Seq[String]): Unit = {
    if (args.isEmpty) {
      message.reply("You need to provide a prefix.")
      return
    } else if (args.size > 1) {
      message.reply("Too many arguments.")
      return
    }
    val prefix = args.head
    if (pending.get(message.getGuild).isDefined && pending(message.getGuild)==prefix) {
      message.getGuild.getData.customPrefix = Some(prefix)
      message.reply(s"Prefix changed to $prefix.")
    } else {
      //prompt
      val currentPrefix = if (message.getGuild.customPrefix.isDefined) message.getGuild.customPrefix.get else "nothing"
      val extra = "**You have the default prefix disabled, and it may be possible to make the bot unusable by setting the prefix" +
        "to an invalid character. Proceed with caution.**"
      pending.put(message.getGuild, prefix)
      message.reply(s"Are you sure you want to change the prefix from " +
        s"`$currentPrefix` to `$prefix`?\nIf so, run this command again. " +
        s"${if (message.getGuild.apply(GuildSetting.disableDefaultPrefix)) extra else ""}")
    }
  }
}
