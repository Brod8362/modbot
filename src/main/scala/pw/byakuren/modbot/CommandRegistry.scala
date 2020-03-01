package pw.byakuren.modbot

import pw.byakuren.modbot.commands.Command

import scala.collection.mutable

class CommandRegistry[T <: Command](val commands: Set[T]) {

  val commandMap: Map[String, T] = {
    val t = new mutable.HashMap[String, T]
    for (c <- commands)
      for (a <- c.aliases)
        t.put(a, c)
    t.toMap
  }

  def apply(name: String): Option[T] = commandMap.get(name)
}
