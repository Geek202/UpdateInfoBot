package me.geek.tom.mcupdateinfo.config.spec

import com.uchuhimo.konf.ConfigSpec

object BotSpec : ConfigSpec() {

    val token by required<String>()
    val commandPrefix by required<String>()

}
