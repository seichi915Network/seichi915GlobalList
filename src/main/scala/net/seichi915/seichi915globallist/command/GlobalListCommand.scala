package net.seichi915.seichi915globallist.command

import java.lang
import java.util.Collections
import net.md_5.bungee.api.{ChatColor, CommandSender}
import net.md_5.bungee.api.chat.{
  ClickEvent,
  ComponentBuilder,
  HoverEvent,
  TextComponent
}

import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.{Command, TabExecutor}
import net.seichi915.seichi915globallist.Seichi915GlobalList
import net.seichi915.seichi915globallist.configuration.Configuration
import net.seichi915.seichi915globallist.util.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class GlobalListCommand
    extends Command("globallist", null, "glist")
    with TabExecutor {
  override def execute(sender: CommandSender, args: Array[String]): Unit = {
    if (!args.isEmpty && args.length != 1) {
      sender.sendMessage(
        TextComponent.fromLegacyText("コマンドの使用法が間違っています。".toErrorMessage): _*)
      return
    }
    val page =
      if (args.isEmpty) 1
      else {
        args(0).toIntOption match {
          case Some(int) => int
          case None =>
            sender.sendMessage(
              TextComponent
                .fromLegacyText("ページは数字で指定してください。".toErrorMessage): _*)
            return
        }
      }
    val serverInfoList =
      Seichi915GlobalList.instance.getProxy.getServers
        .values()
        .asScala
        .filter(_.canAccess(sender))
    if (serverInfoList.isEmpty) {
      sender.sendMessage(
        TextComponent.fromLegacyText("サーバーは見つかりませんでした。".toErrorMessage): _*)
      return
    }
    var pageCount = 1
    val serverInfoCount = serverInfoList.size
    var serverInfoCountClone = serverInfoCount
    while ({
      serverInfoCountClone -= 8
      serverInfoCountClone
    } > 0) pageCount += 1
    if (page > pageCount || page <= 0) {
      sender.sendMessage(
        TextComponent.fromLegacyText("そのページは存在しません。".toErrorMessage): _*)
      return
    }
    val sortedServerNameList = serverInfoList.map(_.getName).toList.sorted
    val serverInfoToDisplay = {
      var list = List[ServerInfo]()
      for (i <- 0 to (if (sortedServerNameList.drop((page - 1) * 8).size < 7)
                        sortedServerNameList.drop((page - 1) * 8).size - 1
                      else 7))
        list = list.appended(
          Seichi915GlobalList.instance.getProxy
            .getServerInfo(sortedServerNameList.drop((page - 1) * 8)(i)))
      list
    }
    Future {
      sender.sendMessage(
        TextComponent.fromLegacyText(
          "情報を取得しています。しばらくお待ちください...".toNormalMessage): _*)
      val translatedServerNameMap = serverInfoToDisplay
        .map(
          serverInfo =>
            serverInfo -> Configuration.getTranslatedServerName(
              serverInfo.getName))
        .toMap
      val onlineStatusMap = serverInfoToDisplay
        .map(serverInfo => serverInfo -> serverInfo.isOnline)
        .toMap
      val playerListMap = serverInfoToDisplay
        .map(
          serverInfo =>
            serverInfo -> (if (onlineStatusMap(serverInfo))
                             serverInfo.getPlayers.asScala.toList
                           else List()))
        .toMap
      val sortedPlayerNameListMap = serverInfoToDisplay
        .map(
          serverInfo =>
            serverInfo -> (if (onlineStatusMap(serverInfo))
                             serverInfo.getPlayers.asScala.toList
                               .map(_.getName)
                               .sorted
                           else List()))
        .toMap
      val totalPlayerCount =
        Seichi915GlobalList.instance.getProxy.getOnlineCount
      sender.sendMessage(TextComponent.fromLegacyText(
        s"${ChatColor.GREEN}seichi915Networkプレイヤーリスト${ChatColor.RESET}($page/${pageCount}ページ):"): _*)
      serverInfoToDisplay.foreach { serverInfo =>
        val translatedServerName = translatedServerNameMap(serverInfo)
        val isOnline = onlineStatusMap(serverInfo)
        val playerList = playerListMap(serverInfo)
        val sortedPlayerNameList = sortedPlayerNameListMap(serverInfo)
        var playerListComponent = new ComponentBuilder()
        val serverNameComponent = new ComponentBuilder()
        if (playerList.nonEmpty) {
          sortedPlayerNameList.foreach { name =>
            playerListComponent = playerListComponent
              .append(TextComponent.fromLegacyText(
                s"${ChatColor.RESET}${if (!sortedPlayerNameList.head.equalsIgnoreCase(name)) ", "
                else ""}${if (name.equalsIgnoreCase(sender.getName)) ChatColor.YELLOW
                else ""}$name"))
              .event(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(
                  s"${if (name.equalsIgnoreCase(sender.getName)) s"${ChatColor.YELLOW}$name"
                  else
                    s"$name\nメッセージを送信: ${sender match {
                      case player: ProxiedPlayer =>
                        if (player.getServer.getInfo.getPlayers.contains(Seichi915GlobalList.instance.getProxy.getPlayer(name))) s"/tell $name"
                        else s"/gtell $name"
                      case _ => s"/gtell $name"
                    }}"}")
              ))
            if (!name.equalsIgnoreCase(sender.getName))
              playerListComponent = playerListComponent
                .event(
                  new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    s"${sender match {
                      case player: ProxiedPlayer =>
                        if (player.getServer.getInfo.getPlayers.contains(Seichi915GlobalList.instance.getProxy
                              .getPlayer(name))) s"/tell $name"
                        else s"/gtell $name"
                      case _ => s"/gtell $name"
                    }}"
                  ))
          }
        } else {
          playerListComponent = playerListComponent
            .append(TextComponent.fromLegacyText("ログインなし"))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                  new Text("このサーバーには現在誰もログインしていません。")))
        }
        translatedServerName match {
          case Some(name) =>
            serverNameComponent
              .append(TextComponent.fromLegacyText(
                s"$name${ChatColor.YELLOW}(${serverInfo.getName})${ChatColor.RESET}"))
          case None =>
            serverNameComponent
              .append(
                TextComponent.fromLegacyText(
                  s"${serverInfo.getName}${ChatColor.RESET}"))
        }
        val serverInfoComponent = new ComponentBuilder()
          .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text(s"${if (isOnline) "稼働中" else "ダウン"}")))
          .append(TextComponent.fromLegacyText(
            s"${if (isOnline) ChatColor.GREEN else ChatColor.RED}●${ChatColor.RESET} "))
          .append("")
          .event(null.asInstanceOf[HoverEvent])
          .append(TextComponent.fromLegacyText(
            s"[人数: ${playerList.length}]${ChatColor.RESET} "))
          .event(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(s"このサーバーへ移動: \n/server ${serverInfo.getName}")))
          .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                s"/server ${serverInfo.getName}"))
          .append(serverNameComponent.create())
          .event(null.asInstanceOf[HoverEvent])
          .event(null.asInstanceOf[ClickEvent])
          .append(TextComponent.fromLegacyText(
            s"${ChatColor.RESET} ${ChatColor.GREEN}≫${ChatColor.RESET} "))
          .append(playerListComponent.create())
        sender.sendMessage(serverInfoComponent.create(): _*)
      }
      sender.sendMessage(
        TextComponent.fromLegacyText(
          s"${ChatColor.GREEN}総ログイン数: ${ChatColor.RESET}$totalPlayerCount"): _*)
    } onComplete {
      case Success(_) =>
      case Failure(exception) =>
        exception.printStackTrace()
        sender.sendMessage(
          TextComponent.fromLegacyText("エラーが発生しました。".toErrorMessage): _*)
    }
  }

  override def onTabComplete(sender: CommandSender,
                             args: Array[String]): lang.Iterable[String] =
    if (args.length == 1) {
      val serverInfoList =
        Seichi915GlobalList.instance.getProxy.getServers
          .values()
          .asScala
          .filter(_.canAccess(sender))
      if (serverInfoList.isEmpty) return Collections.emptyList()
      var pageCount = 1
      val serverInfoCount = serverInfoList.size
      var serverInfoCountClone = serverInfoCount
      while ({
        serverInfoCountClone -= 8
        serverInfoCountClone
      } > 0) pageCount += 1
      (1 to pageCount).map(_.toString).asJava
    } else Collections.emptyList()
}
