package pw.byakuren.modbot

import net.dv8tion.jda.api.entities.Guild

import scala.collection.mutable

class GuildDataManager {

  private val dataMap = new mutable.HashMap[Guild, GuildData]

  def apply(guild: Guild): GuildData = {
    dataMap.get(guild) match {
      case None =>
        dataMap.put(guild, new GuildData(guild, None, None))
      case _ =>
    }
    dataMap(guild)
  }
}
