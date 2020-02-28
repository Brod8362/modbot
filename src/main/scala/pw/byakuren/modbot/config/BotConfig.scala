package pw.byakuren.modbot.config

import java.io.File
import java.util.Scanner

import pw.byakuren.modbot.config.DataType.DataType

import scala.collection.mutable

class BotConfig(val file: File) {

  val map: mutable.Map[String, Any] = new mutable.HashMap[String, Any]
  parseFile(file)

  private def parseFile(file: File): Unit = {
    val scanner = new Scanner(file)
    var line = 0
    while (scanner.hasNextLine) {
      val data = scanner.nextLine().split("=", 2)
      if (data.size<2 || determineType(data(1))==DataType.Unknown) throw new RuntimeException(f"Invalid config file syntax at line $line")
      map.put(data(0), determineType(data(1)) match {
        case DataType.String => data(1).substring(1, data(1).length - 1)
        case DataType.Int => data(1).toInt
        case DataType.Float => data(1).toFloat
        case DataType.Long => data(1).substring(0, data(1).length - 1).toLong
        case _ => None
      })
      line+=1
    }
  }

  private def determineType(s: String): DataType = {
    if (s.matches("\\d+L")) return DataType.Long
    if (s.matches("\\d+\\.\\d+")) return DataType.Float
    if (s.matches("\\d+")) return DataType.Int
    if (s.matches("\".*\"")) return DataType.String
    DataType.Unknown
  }

  override def toString: String = {
    map.mkString(",")
  }

  def getString(key: String): Option[String] = {
    Option(map(key).asInstanceOf[String])
  }

  def getInt(key: String): Option[Int] = {
    Option(map(key).asInstanceOf[Int])
  }

  def getLong(key: String): Option[Long] = {
    Option(map(key).asInstanceOf[Long])
  }

  def getFloat(key: String): Option[Float] = {
    Option(map(key).asInstanceOf[Float])
  }
}
