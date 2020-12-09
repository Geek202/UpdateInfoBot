package me.geek.tom.mcupdateinfo.ext

import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension

class UtilsExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Utils"
    override suspend fun setup() {
        command {
            name = "emojidump"
            check {
                it.message.channel.asChannel() is GuildChannel
            }
            action {
                val channel = message.channel.asChannel() as GuildChannel
                for (emoji in channel.guild.getPreview().emojis) {
                    message.channel.createMessage("${emoji.name}: ${emoji.mention}")
                }
            }
        }
    }
}