package pw.byakuren.modbot.guild

import net.dv8tion.jda.api.entities.Guild
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}
import pw.byakuren.modbot.guild.GuildSetting.GuildSetting

case object GuildSetting extends Enumeration {
  type GuildSetting = Int
  val reportingEnabled = 1
  val enableFAQ = 2
  val useCustomPrefix = 3
  val disableDefaultPrefix = 4
}

class GuildSettings(val guild: Guild, private var bitfield: Int) extends SQLWritable {

  def update(guildSetting: GuildSetting, boolean: Boolean): Unit = {
    if (boolean)
      bitfield = bitfield | (1 << guildSetting)
    else
      bitfield = bitfield & ~(1 << guildSetting)
  }

  def apply(guildSetting: GuildSetting): Boolean = ((bitfield >> guildSetting) & 1) == 1

  def value: Int = bitfield

  override def write(SQLConnection: SQLConnection): Unit = {
    SQLConnection.writeGuildSettings(this)
  }
}

