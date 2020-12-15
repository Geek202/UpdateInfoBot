package me.geek.tom.mcupdateinfo.config

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.GuildEmoji
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import me.geek.tom.mcupdateinfo.bot
import me.geek.tom.mcupdateinfo.config.spec.BotSpec
import me.geek.tom.mcupdateinfo.config.spec.EmojiSpec
import me.geek.tom.mcupdateinfo.config.spec.UpdateAnalysisSpec
import me.geek.tom.mcupdateinfo.config.spec.UpdateCheckerSpec
import java.io.File

class BotConfig internal constructor() {

    private var config = Config {
        addSpec(BotSpec)
        addSpec(UpdateCheckerSpec)
        addSpec(EmojiSpec)
        addSpec(UpdateAnalysisSpec)
    }
        .from.toml.resource("default.toml", false)
        .from.toml.watchFile("config.toml")

    // Update checker

    val launchMetaBase: String get() = config[UpdateCheckerSpec.launcherMetaBase]
    suspend fun getUpdateAlertsChannel(): GuildMessageChannel {
        return bot.kord.getChannel(Snowflake(config[UpdateCheckerSpec.updateChannel]),
            EntitySupplyStrategy.cacheWithRestFallback) as GuildMessageChannel
    }

    // Bot
    val token: String get() = config[BotSpec.token]
    val prefix: String get() = config[BotSpec.commandPrefix]
    val owner: Snowflake get() = Snowflake(config[BotSpec.owner])
    val dataDir = File("version_data")

    // Emoji
    suspend fun getEmojiGuild(): Guild {
        return bot.kord.getGuild(Snowflake(config[EmojiSpec.emojiGuild]), EntitySupplyStrategy.cacheWithRestFallback)!!
    }

    suspend fun getEmoji(emoji: Emoji): GuildEmoji {
        return getEmojiGuild().getEmoji(Snowflake(config[emoji.configItem]))
    }

    // Update analysis
    suspend fun getAnalysisOutputChannel(): GuildMessageChannel {
        return bot.kord.getChannel(Snowflake(config[UpdateAnalysisSpec.analysisOutputChannel]),
            EntitySupplyStrategy.cacheWithRestFallback) as GuildMessageChannel
    }

    init {
        if (!dataDir.exists())
            dataDir.mkdirs()
    }
}

val botConfig = BotConfig()
