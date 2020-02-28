package pw.byakuren.modbot

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.{Guild, Member, MessageChannel, Role}

class GuildData(val server: Guild, var logChannel: Option[MessageChannel], var moderatorRole: Option[Role] = None) {

  def isAdmin(member: Member): Boolean = {
    for (role <- moderatorRole) {
      if (member.getRoles.contains(role)) return true
    }
    member.hasPermission(Permission.ADMINISTRATOR)
  }

}
