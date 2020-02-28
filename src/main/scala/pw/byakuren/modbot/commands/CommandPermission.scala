package pw.byakuren.modbot.commands

case object CommandPermission extends Enumeration {
  type CommandPermission = Value
  val Everybody, Private, Admins, Debug = Value
}
