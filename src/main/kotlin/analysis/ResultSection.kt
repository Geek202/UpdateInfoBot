package me.geek.tom.mcupdateinfo.analysis

import com.mojang.serialization.Codec
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion

interface ResultSection<T : ResultSection<T>> {
    fun toDiscordString(): String
    fun getCodec(version: MinecraftVersion): Codec<T>
    fun getTitle(): String
}
