package me.geek.tom.mcupdateinfo.config.spec

import com.uchuhimo.konf.ConfigSpec

object UpdateAnalysisSpec : ConfigSpec() {
    val analysisOutputChannel by required<Long>()
}
