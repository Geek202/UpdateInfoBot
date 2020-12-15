package me.geek.tom.mcupdateinfo.analysis

import me.geek.tom.mcdiffer.json.MinecraftVersionJson
import java.nio.file.FileSystem

interface Analyser<T, R : ResultSection<R>> {

    fun analyse(fs: FileSystem, phase: Phase) { }
    fun analyse(item: T, phase: Phase) { }
    fun analyseManifest(manifest: MinecraftVersionJson) { }

    fun completeAnalysis(): ResultSection<R>
    fun resetState()

    fun getTitle(): String
    fun getPhases(): Set<Phase>

    enum class Phase {
        CLIENT, SERVER, MERGED, MAPPED
    }
}
