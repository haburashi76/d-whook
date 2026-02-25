package io.github.haburashi76.d_whook



import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

class DWhookPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        saveDefaultConfig()

        val cfile = File(dataFolder, "config.yml")

        if (cfile.length() == 0L) {
            config.options().copyDefaults(true)
            saveConfig()
        }

        getCommand("dwhook")?.setTabCompleter { _, _, _, strings ->
            if (strings.size == 1) return@setTabCompleter listOf("reload")
            else listOf()
        }
        getCommand("dwhook")?.setExecutor { sender, _, _, strings ->
            if (strings.size == 1 && strings[0] == "reload") {
                try {
                    reloadConfig()
                    sender.sendMessage("config.yml 다시 불러오기 성공!")
                } catch (e: Exception) {
                    sender.sendMessage("config.yml 다시 불러오기 실패")
                    e.printStackTrace()
                }

                return@setExecutor true
            }
            return@setExecutor false
        }
    }

    override fun onDisable() {
        if (server.isStopping) {
            getSetting("server-stop-message")?.let {
                sendMessage(
                    it
                        .replace("\$Y\$", "${LocalDate.now().year}")
                        .replace("\$M\$", "${LocalDate.now().monthValue}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$D\$", "${LocalDate.now().dayOfMonth}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$H\$", "${LocalTime.now().hour}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$Min\$", "${LocalTime.now().minute}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$S\$", "${LocalTime.now().second}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                )
            }
        }
    }

    private fun getSetting(path: String): String? {
        return config.getString(path)
    }

    @EventHandler
    fun onServerStart(event: ServerLoadEvent) {
        if (event.type == ServerLoadEvent.LoadType.STARTUP) {
            getSetting("server-start-message")?.let {
                sendMessage(
                    it
                        .replace("\$Y\$", "${LocalDate.now().year}")
                        .replace("\$M\$", "${LocalDate.now().monthValue}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$D\$", "${LocalDate.now().dayOfMonth}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$H\$", "${LocalTime.now().hour}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$Min\$", "${LocalTime.now().minute}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                        .replace("\$S\$", "${LocalTime.now().second}".let { m ->
                            (if (m.length == 1) "0" else "") + m
                        })
                )
            }
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
            Registry.SOUNDS.get(NamespacedKey("minecraft", it.lowercase()))?.let { sound ->
                server.onlinePlayers.forEach { player ->
                    player.playSound(player, sound, 2.0f, 1.0f)
                }
                event.player.playSound(event.player, sound, 2.0f, 1.0f)
            }
        }

        getSetting("player-join-message")?.let {
            sendMessage(
                it
                    .replace("\$name\$", event.player.name)
                    .replace("\$Y\$", "${LocalDate.now().year}")
                    .replace("\$M\$", "${LocalDate.now().monthValue}".let { m ->
                        (if (m.length == 1) "0" else "") + m
                    })
                    .replace("\$D\$", "${LocalDate.now().dayOfMonth}".let { m ->
                        (if (m.length == 1) "0" else "") + m
                    })
                    .replace("\$H\$", "${LocalTime.now().hour}".let { m ->
                        (if (m.length == 1) "0" else "") + m
                    })
                    .replace("\$Min\$", "${LocalTime.now().minute}".let { m ->
                        (if (m.length == 1) "0" else "") + m
                    })
                    .replace("\$S\$", "${LocalTime.now().second}".let { m ->
                        (if (m.length == 1) "0" else "") + m
                    })
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        getSetting("player-leave-sound")?.let {
            Registry.SOUNDS.get(NamespacedKey("minecraft", it.lowercase()))?.let { sound ->
                server.onlinePlayers.forEach { player ->
                    player.playSound(player, sound, 2.0f, 1.0f)
                }
                event.player.playSound(event.player, sound, 2.0f, 1.0f)
            }
        }
        getSetting("player-leave-message")?.let {
            sendMessage(it
                .replace("\$name\$", event.player.name)
                .replace("\$Y\$", "${LocalDate.now().year}")
                .replace("\$M\$", "${LocalDate.now().monthValue}".let { m ->
                    (if (m.length == 1) "0" else "") + m
                })
                .replace("\$D\$", "${LocalDate.now().dayOfMonth}".let { m ->
                    (if (m.length == 1) "0" else "") + m
                })
                .replace("\$H\$", "${LocalTime.now().hour}".let { m ->
                    (if (m.length == 1) "0" else "") + m
                })
                .replace("\$Min\$", "${LocalTime.now().minute}".let { m ->
                    (if (m.length == 1) "0" else "") + m
                })
                .replace("\$S\$", "${LocalTime.now().second}".let { m ->
                    (if (m.length == 1) "0" else "") + m
                })
            )
        }
    }
}


