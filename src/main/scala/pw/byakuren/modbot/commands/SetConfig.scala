package pw.byakuren.modbot.commands

import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class SetConfig(implicit guildDataManager: GuildDataManager) extends GuildCommand(Seq("conf"), "", "",
  CommandPermission.Everybody) {
  override def run(message: Message, args: Seq[String]): Unit = {
    if (args.size != 2)
      message.reply(f"${message.getGuild.getSettings.value} (${message.getGuild.getSettings.value.toBinaryString})")
    else {
      val i = args.head.toInt
      val b = args.last.asBoolean
      message.getGuild.getSettings.update(i,b)
      run(message, Nil)
    }
  }
}
