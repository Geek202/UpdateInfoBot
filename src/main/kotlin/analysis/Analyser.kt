package me.geek.tom.mcupdateinfo.analysis

import java.io.File
import java.nio.file.FileSystem

interface Analyser<T, R : ResultSection<R>> {

    fun analyse(fs: FileSystem)

    fun analyse(item: T)
    fun completeAnalysis(): ResultSection<R>
    fun resetState()

    fun getTitle(): String
    fun getPhase(): Phase

    enum class Phase {
        CLIENT, SERVER, MERGED, MAPPED
    }
}
