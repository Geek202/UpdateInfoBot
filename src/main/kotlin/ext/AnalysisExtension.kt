package me.geek.tom.mcupdateinfo.ext

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import me.geek.tom.mcupdateinfo.analysis.OKHTTP
import me.geek.tom.mcupdateinfo.analysis.VersionAnalyser
import me.geek.tom.mcupdateinfo.botEmbed
import me.geek.tom.mcupdateinfo.config.botConfig
import me.geek.tom.mcupdateinfo.event.MinecraftUpdateEvent
import me.geek.tom.mcupdateinfo.util.LoggingMessage

class AnalysisExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "Version Analysis"

    override suspend fun setup() {
        event<MinecraftUpdateEvent>() {
            action { event ->
                val version = event.newVersion
                val loadingMessage = LoggingMessage(bot, "Starting analysis of ${version.id}")
                loadingMessage.send(botConfig.getAnalysisOutputChannel())

                val analyser = VersionAnalyser(version.getVersionInfo(OKHTTP))
                val results = analyser.analyse(loadingMessage)
                for (result in results) {
                    botConfig.getAnalysisOutputChannel().botEmbed {
                        title = result.getTitle()
                        description = result.toDiscordString()
                    }
                }

                loadingMessage.complete()
            }
        }
    }
}
