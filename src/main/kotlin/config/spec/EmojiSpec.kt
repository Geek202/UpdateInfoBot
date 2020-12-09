package me.geek.tom.mcupdateinfo.config.spec

import com.uchuhimo.konf.ConfigSpec

object EmojiSpec : ConfigSpec() {
    val emojiGuild by required<Long>()
    val loadingEmoji by required<Long>()
}
