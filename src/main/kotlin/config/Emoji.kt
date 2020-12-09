package me.geek.tom.mcupdateinfo.config

import com.uchuhimo.konf.RequiredItem
import me.geek.tom.mcupdateinfo.config.spec.EmojiSpec

enum class Emoji(val configItem: RequiredItem<Long>) {

    LOADING(EmojiSpec.loadingEmoji)

}
