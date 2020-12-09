package me.geek.tom.mcupdateinfo.config.spec

import com.uchuhimo.konf.ConfigSpec

object UpdateCheckerSpec : ConfigSpec() {
    val launcherMetaBase by required<String>()
    val updateChannel by required<Long>()
}
