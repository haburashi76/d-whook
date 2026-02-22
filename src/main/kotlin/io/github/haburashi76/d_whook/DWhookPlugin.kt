package io.github.haburashi76.d_whook


import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class DWhookPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        saveDefaultConfig()

        val cfile = File(dataFolder, "config.yml")

        if (cfile.length() == 0L) {
            config.options().copyDefaults(true)
            saveConfig()
        }

        getCommand("dwhook")?.run {
            setTabCompleter { _, _, _, strings ->
                if (strings.size == 1) return@setTabCompleter listOf("reload")
                else listOf()
            }
            setExecutor { _, _, _, strings ->
                if (strings.size == 1 && strings[0] == "reload") {
                    reloadConfig()
                    return@setExecutor true
                }
                return@setExecutor false
            }
        }
    }

    override fun onDisable() {
        if (server.isStopping) {
            getSetting("server-stop-message")?.let { sendMessage(it) }
        }
    }

    private fun getSetting(path: String): String? {
        return config.getString(path)
    }

    @EventHandler
    fun onServerStart(event: ServerLoadEvent) {
        if (event.type == ServerLoadEvent.LoadType.STARTUP) {
            getSetting("server-start-message")?.let { sendMessage(it) }
        }
    }

    private fun sendMessage(content: String) {
        val url = getSetting("webhook-url")?: return
        val name = getSetting("sender-name")?: "[Server]"

        val webhook = DiscordWebhook(url)

        webhook.setUsername(name)
        webhook.setContent(content)

        try {
            webhook.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        getSetting("player-join-sound")?.let {
            server.onlinePlayers.forEach { player ->
                val sound = Sound.sound()
                    .pitch(1.0f)
                    .volume(2.0f)
                    .source(Sound.Source.PLAYER)
                    .type(Key.key({ "minecraft" }, it))
                    .build()
                player.playSound(sound)
            }
        }
        getSetting("player-join-message")?.let { sendMessage(it) }
    }
}


