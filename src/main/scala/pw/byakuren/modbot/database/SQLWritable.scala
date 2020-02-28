package pw.byakuren.modbot.database

trait SQLWritable {

  def write(SQLConnection: SQLConnection): Boolean

}
