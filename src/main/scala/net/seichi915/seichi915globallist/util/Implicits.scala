package net.seichi915.seichi915globallist.util

import java.net.Socket

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.config.ServerInfo

object Implicits {
  implicit class AnyOps(any: Any) {
    def isNull: Boolean = Option(any).flatMap(_ => Some(false)).getOrElse(true)

    def nonNull: Boolean = !isNull
  }

  implicit class StringOps(string: String) {
    def toNormalMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.WHITE}seichi915GlobalList${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toSuccessMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GREEN}seichi915GlobalList${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toWarningMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GOLD}seichi915GlobalList${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toErrorMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.RED}seichi915GlobalList${ChatColor.AQUA}]${ChatColor.RESET} $string"
  }

  implicit class ServerInfoOps(serverInfo: ServerInfo) {
    def isOnline: Boolean =
      try {
        val hostname = serverInfo.getSocketAddress.toString
          .split(":")
          .dropRight(1)
          .mkString(":")
          .split("/")
          .head
        val port = serverInfo.getSocketAddress.toString.split(":").last.toInt
        val socket = new Socket(hostname, port)
        socket.close()
        true
      } catch {
        case _: Exception =>
          false
      }
  }
}
