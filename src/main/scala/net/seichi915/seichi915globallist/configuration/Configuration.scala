package net.seichi915.seichi915globallist.configuration

import java.io.File
import java.nio.file.Files

import net.md_5.bungee.config.{ConfigurationProvider, YamlConfiguration}
import net.seichi915.seichi915globallist.Seichi915GlobalList
import net.seichi915.seichi915globallist.util.Implicits._

object Configuration {
  private var configuration: net.md_5.bungee.config.Configuration = _

  def saveDefaultConfig: Boolean = {
    if (!Seichi915GlobalList.instance.getDataFolder.exists())
      Seichi915GlobalList.instance.getDataFolder.mkdir()
    val configFile =
      new File(Seichi915GlobalList.instance.getDataFolder, "config.yml")
    if (!configFile.exists()) {
      try {
        val inputStream =
          Seichi915GlobalList.instance.getResourceAsStream("config.yml")
        Files.copy(inputStream, configFile.toPath)
        inputStream.close()
        true
      } catch {
        case e: Exception =>
          e.printStackTrace()
          false
      }
    } else true
  }

  def load: Boolean = {
    val configFile =
      new File(Seichi915GlobalList.instance.getDataFolder, "config.yml")
    if (!configFile.exists()) return false
    try {
      configuration = ConfigurationProvider
        .getProvider(classOf[YamlConfiguration])
        .load(configFile)
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def getTranslatedServerName(original: String): Option[String] = {
    val serverName = configuration.getString(s"TranslatedServerNames.$original")
    if (serverName.isNull || serverName.isEmpty) return None
    Some(serverName)
  }
}
