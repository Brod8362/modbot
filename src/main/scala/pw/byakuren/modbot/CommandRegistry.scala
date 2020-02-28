package pw.byakuren.modbot

import pw.byakuren.modbot.commands.Command

import scala.collection.mutable

class CommandRegistry(val commands: Set[Command]) {

  val commandMap: Map[String, Command] = {
    val t = new mutable.HashMap[String, Command]
    for (c <- commands)
      for (a <- c.aliases)
        t.put(a, c)
    t.toMap
  }

  def apply(name: String): Option[Command] = commandMap.get(name)
}
