package me.geek.tom.mcupdateinfo.event

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.events.ExtensionEvent
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion

class MinecraftUpdateEvent(
    override val bot: ExtensibleBot,
    val newVersion: MinecraftVersion
) : ExtensionEvent
