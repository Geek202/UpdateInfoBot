package me.geek.tom.mcupdateinfo

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.gateway.Intents
import com.gitlab.kordlib.gateway.PrivilegedIntent
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import com.gitlab.kordlib.rest.builder.message.MessageModifyBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kotlindiscord.kord.extensions.ExtensibleBot
import me.geek.tom.mcupdateinfo.config.botConfig
import me.geek.tom.mcupdateinfo.config.buildInfo
import me.geek.tom.mcupdateinfo.ext.AnalysisExtension
import me.geek.tom.mcupdateinfo.ext.UtilsExtension
import me.geek.tom.mcupdateinfo.ext.VersionCheckExtension
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val bot = ExtensibleBot(
    prefix = botConfig.prefix,
    token = botConfig.token,
    addHelpExtension = true
)

val LOGGER: Logger = LoggerFactory.getLogger("Main")
val GSON: Gson = GsonBuilder().create()

@OptIn(PrivilegedIntent::class)
suspend fun main(args: Array<String>) {
    // Redirect system.out/err to a Log4j2 logger.
    val outLogger = LogManager.getLogger("System")
    System.setOut(IoBuilder.forLogger(outLogger).setLevel(Level.INFO).buildPrintStream())
    System.setErr(IoBuilder.forLogger(outLogger).setLevel(Level.ERROR).buildPrintStream())

    bot.addExtension(VersionCheckExtension::class)
    bot.addExtension(AnalysisExtension::class)
    if (buildInfo.isDev())
        bot.addExtension(UtilsExtension::class)

    LOGGER.info("Starting bot...")
    bot.start(
        presenceBuilder = {
            playing("${botConfig.prefix}help for command help")
        },

        intents = {
            +Intents.all
        }
    )
}

fun botInfo(): String {
    return "McUpdateInfo ${buildInfo.version}-${buildInfo.environment}"
}

suspend fun Message.botEmbed(cfg: EmbedBuilder.() -> Unit): Message {
    val a = author
    return channel.createEmbed {
        footer {
            text = botInfo()
            icon = a?.avatar?.getUrl(Image.Format.WEBP)
        }
        cfg(this)
    }
}

suspend fun MessageChannelBehavior.botEmbed(cfg: EmbedBuilder.() -> Unit): Message {
    return createEmbed {
        footer {
            text = botInfo()
        }
        cfg(this)
    }
}

fun MessageModifyBuilder.botEmbed(cfg: EmbedBuilder.() -> Unit) {
    embed {
        footer {
            text = botInfo()
        }
        cfg(this)
    }
}
