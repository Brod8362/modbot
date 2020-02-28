package pw.byakuren.modbot.database

import java.io.{File, FileNotFoundException}

import net.dv8tion.jda.api.entities.{Guild, TextChannel}
import scalikejdbc._

class SQLConnection {
  if (!new File("database.db").exists()) {
    throw new FileNotFoundException("Database file does not exist")
  }
  Class.forName("org.sqlite.JDBC")
  ConnectionPool.singleton("jdbc:sqlite:modbot.db", null, null)

  implicit val session: AutoSession.type = AutoSession

  sql"CREATE TABLE IF NOT EXISTS log_channel (guild INTEGER PRIMARY KEY NOT NULL, channel INTEGER NOT NULL)".execute().apply()

  def setGuildLogChannel(channel: TextChannel): Boolean = {
    sql"INSERT OR REPLACE INTO log_channel VALUES (${channel.getGuild.getIdLong}, ${channel.getIdLong})".execute().apply()
  }

  def getGuildLogChannel(guild: Guild): Option[TextChannel] = {
    sql"SELECT channel FROM log_channel WHERE guild=${guild.getIdLong}"
      .map(_.long("channel")).single().apply() match {
      case Some(id) =>
        Some(guild.getTextChannelById(id))
      case _ => None
    }
  }
}
