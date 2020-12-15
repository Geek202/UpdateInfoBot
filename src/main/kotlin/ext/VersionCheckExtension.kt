package me.geek.tom.mcupdateinfo.ext

import com.gitlab.kordlib.common.annotation.KordPreview
import com.gitlab.kordlib.common.entity.ChannelType
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.enum
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.geek.tom.mcdiffer.fetchJson
import me.geek.tom.mcdiffer.json.MinecraftVersionJson
import me.geek.tom.mcupdateinfo.GSON
import me.geek.tom.mcupdateinfo.botEmbed
import me.geek.tom.mcupdateinfo.config.botConfig
import me.geek.tom.mcupdateinfo.config.buildInfo
import me.geek.tom.mcupdateinfo.event.MinecraftUpdateEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import ru.gildor.coroutines.okhttp.await
import java.awt.Color

private const val CHECK_DELAY = 1000L * 30L
private const val STARTUP_DELAY = 1000L * 5L
private val LOGGER = LoggerFactory.getLogger("VersionChecker")

class VersionCheckExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "Version Checker"

    private var checking = false
    private var versions: LauncherMetaResponse? = null
    private var checkJob: Job? = null

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                delay(STARTUP_DELAY)

                versions = fetchVersions()
                LOGGER.info("Initial version data has been fetched! Starting update check loop...")
                checkJob = bot.kord.launch {
                    while (true) {
                        delay(CHECK_DELAY)
                        try {
                            updateCheck()
                        } catch (e: Throwable) {
                            LOGGER.warn("Failed to run update check!", e)
                        }
                    }
                }
            }
        }

        command {
            name = "latest"
            description = "Get the latest version of Minecraft, snapshot or otherwise!"

            signature(::VersionArguments)

            action {
                with(parse(::VersionArguments)) {
                    if (versions == null) {
                        message.botEmbed {
                            color = Color.RED
                            description = "Versions have not been fetched yet, check back soon!"
                        }
                        return@action
                    }

                    message.botEmbed {
                        color = Color.GREEN
                        description = ""
                        title = "Latest Minecraft ${type.name.toLowerCase()} version: ${versions?.latest?.get(type)?: "unknown"}"
                    }
                }
            }
        }

        command {
            name = "update"
            description = "debug command to trigger version analysis"

            signature(::VersionArguments)

            check {
                buildInfo.isDev() || it.member?.id == botConfig.owner
            }

            action {
                with(parse(::VersionArguments)) {
                    if (versions == null) {
                        message.botEmbed {
                            color = Color.RED
                            description = "Versions have not been fetched yet, check back soon!"
                        }
                        return@action
                    }

                    val versionId = versions!!.latest.get(type)
                    val version = versions!!.versions.find { it.id == versionId }
                    if (version == null) {
                        message.botEmbed {
                            color = Color.RED
                            description = "Uh oh. Looks like the latest version doesn't exist ¯\\_(ツ)_/¯"
                        }
                    } else {
                        message.botEmbed {
                            color = Color.GREEN
                            description = "Triggering build analysis!"
                        }
                        bot.send(MinecraftUpdateEvent(bot, version))
                    }
                }
            }
        }
    }

    private suspend fun updateCheck() {
        if (checking) {
            LOGGER.warn("Already checking!")
            return
        }

        checking = true
        LOGGER.debug("Checking for Minecraft updates...")
        val versions = fetchVersions()
        val currentVersions = this.versions?.versions?: emptyList()
        val new = versions.versions.find { it !in currentVersions }

        LOGGER.debug("LauncherMeta | Found new version: ${new?: "none"}")
        LOGGER.debug("LauncherMeta | Total version count: ${versions.versions.size}")

        if (new != null) {
            sendUpdateMessage(new)
            bot.send(MinecraftUpdateEvent(bot, new))
        }

        this.versions = versions
        checking = false
    }

    @OptIn(KordPreview::class)
    private suspend fun sendUpdateMessage(new: MinecraftVersion) {
        val channel = botConfig.getUpdateAlertsChannel()
        val message = channel.createMessage(":tada: A new ${new.type} version of Minecraft was released: ${new.id}!")
        if (channel.type == ChannelType.GuildNews) {
            message.publish()
        }
    }

    private suspend fun fetchVersions(): LauncherMetaResponse {
        return LauncherMetaApi.INSTANCE.getVersions().await()
    }

    override suspend fun doUnload() {
        super.doUnload()
        this.checkJob?.cancel()
    }

    class VersionArguments : Arguments() {
        val type by enum<LatestType>("type", "release|snapshot")
    }

    enum class LatestType {
        RELEASE, SNAPSHOT
    }
}

data class MinecraftVersion(
    val id: String,
    val type: String,
    val url: String
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getVersionInfo(client: OkHttpClient): MinecraftVersionJson {
        val req = Request.Builder()
                .url(url)
                .build()
        return GSON.fromJson(client.newCall(req)
                .await().body()?.string(), MinecraftVersionJson::class.java)
    }
}

data class MinecraftLatest(
    val release: String,
    val snapshot: String,
) {
    fun get(type: VersionCheckExtension.LatestType): String {
        return when (type) {
            VersionCheckExtension.LatestType.RELEASE -> release
            VersionCheckExtension.LatestType.SNAPSHOT -> snapshot
        }
    }
}

private data class LauncherMetaResponse(
    val versions: List<MinecraftVersion>,
    val latest: MinecraftLatest
)

private interface LauncherMetaApi {
    @GET("/mc/game/version_manifest.json")
    fun getVersions(): Call<LauncherMetaResponse>

    companion object {
        var INSTANCE: LauncherMetaApi = create(botConfig.launchMetaBase)

        private fun create(base: String): LauncherMetaApi {
            return Retrofit.Builder()
                .baseUrl(base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LauncherMetaApi::class.java)
        }
    }
}
