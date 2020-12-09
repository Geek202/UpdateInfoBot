package me.geek.tom.mcupdateinfo.config

import java.util.*

data class BuildInfo(
    val environment: String,
    val version: String
) {
    fun isDev(): Boolean {
        return environment == "development" || version == ('$' + "{version}")
    }
}

val buildInfo by lazy {
    val props = Properties()
    props.load(BuildInfo::class.java.classLoader.getResourceAsStream("build.properties"))
    BuildInfo(props.getProperty("environment"), props.getProperty("version"))
}
