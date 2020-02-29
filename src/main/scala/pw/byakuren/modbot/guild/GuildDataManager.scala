package pw.byakuren.modbot.guild

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import pw.byakuren.modbot.database.{SQLConnection, SQLWritable}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class GuildDataManager extends SQLWritable {

  private val dataMap = new mutable.HashMap[Guild, GuildData]

  def loadGuilds(jda: JDA, SQLConnection: SQLConnection): Unit = {
    for (guild <- jda.getGuilds.asScala)
      dataMap.put(guild, loadGuildData(guild, SQLConnection))
  }

  def apply(guild: Guild): GuildData = {
    dataMap.get(guild) match {
      case None =>
        dataMap.put(guild, new GuildData(guild, None, None))
      case _ =>
    }
    dataMap(guild)
  }

  def loadGuildData(guild: Guild, sql: SQLConnection): GuildData = {
    new GuildData(guild, sql.getGuildLogChannel(guild), None)
  }

  override def write(SQLConnection: SQLConnection): Unit = {
    dataMap.values.foreach(_.write(SQLConnection))
  }
}