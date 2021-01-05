package net.seichi915.seichi915globallist

import net.md_5.bungee.api.plugin.Plugin
import net.seichi915.seichi915globallist.command._
import net.seichi915.seichi915globallist.configuration.Configuration

object Seichi915GlobalList {
  var instance: Seichi915GlobalList = _
}

class Seichi915GlobalList extends Plugin {
  Seichi915GlobalList.instance = this

  override def onEnable(): Unit = {
    if (!Configuration.saveDefaultConfig) {
      getLogger.severe("デフォルトのconfig.ymlファイルをコピーできませんでした。プロキシを停止します。")
      getProxy.stop()
      return
    }
    if (!Configuration.load) {
      getLogger.severe("config.ymlの読み込みに失敗しました。プロキシを停止します。")
      getProxy.stop()
      return
    }
    Seq(
      new GlobalListCommand
    ).foreach(getProxy.getPluginManager.registerCommand(this, _))

    getLogger.info("seichi915GlobalListが有効になりました。")
  }

  override def onDisable(): Unit = {
    getLogger.info("seichi915GlobalListが無効になりました。")
  }
}
