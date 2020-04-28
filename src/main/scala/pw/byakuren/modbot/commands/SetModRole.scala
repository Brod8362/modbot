package pw.byakuren.modbot.commands
import net.dv8tion.jda.api.entities.Message
import pw.byakuren.modbot.guild.GuildDataManager
import pw.byakuren.modbot.util.Utilities._

class SetModRole(implicit guildDataManager: GuildDataManager) extends GuildCommand(Seq("setmodrole","modrole","smr","mr"),
  "Set the moderator role for the guild.", "", CommandPermission.Admins) {
  override def run(message: Message, args: Seq[String]): Unit = {
    if (message.getMentionedRoles.size != 1) {
      message.reply("Please ping a role to use as your moderator role.")
    } else {
      val role = message.getMentionedRoles.get(0)
      message.reply(f"Set mod role to `${role.getName}` (${role.getId})")
      message.getGuild.getData.moderatorRole = Some(role)
    }
  }
}
