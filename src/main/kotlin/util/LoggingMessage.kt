package me.geek.tom.mcupdateinfo.util

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import com.kotlindiscord.kord.extensions.ExtensibleBot
import kotlinx.coroutines.delay
import me.geek.tom.mcupdateinfo.botEmbed
import me.geek.tom.mcupdateinfo.config.botConfig
import me.geek.tom.mcupdateinfo.config.Emoji

class LoggingMessage(
    private val bot: ExtensibleBot,
    initialMessage: String
) {

    private var currentStatus: String = initialMessage
    private var channel: Snowflake? = null
    private var message: Snowflake? = null

    suspend fun send(channel: MessageChannel) {
        val emoji = botConfig.getEmoji(Emoji.LOADING).mention
        val message = channel.botEmbed {
            description = "$emoji $currentStatus"
        }
        this.message = message.id
        this.channel = message.channelId
    }

    suspend fun updateMessage(newMessage: String) {
        if (channel != null && message != null) {
            val channel = bot.kord.getChannel(this.channel!!, EntitySupplyStrategy.cacheWithRestFallback) as MessageChannel
            currentStatus = newMessage
            channel.getMessage(this.message!!).edit {
                val emoji = botConfig.getEmoji(Emoji.LOADING).mention
                botEmbed {
                    description = "$emoji $currentStatus"
                }
            }
        }
    }

    suspend fun complete() {
        if (channel != null && message != null) {
            val channel = bot.kord.getChannel(this.channel!!, EntitySupplyStrategy.cacheWithRestFallback) as MessageChannel
            val msg = channel.getMessage(this.message!!)
            msg.edit {
                botEmbed {
                    description = "✅ Complete!"
                }
            }
            delay(1000L)
            msg.delete()
        }
    }
}
