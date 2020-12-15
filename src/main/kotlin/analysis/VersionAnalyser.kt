package me.geek.tom.mcupdateinfo.analysis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.geek.tom.mcdiffer.json.MinecraftVersionJson
import me.geek.tom.mcupdateinfo.analysis.impl.ClassCountAnalyser
import me.geek.tom.mcupdateinfo.analysis.impl.FieldSpottingAnalyser
import me.geek.tom.mcupdateinfo.analysis.impl.VersionJsonAnalyser
import me.geek.tom.mcupdateinfo.analysis.impl.VersionManifestAnalyser
import me.geek.tom.mcupdateinfo.config.botConfig
import me.geek.tom.mcupdateinfo.util.LoggingMessage
import me.geek.tom.mcupdateinfo.util.toJarURI
import net.fabricmc.stitch.merge.JarMerger
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import java.util.stream.Collectors

val OKHTTP = OkHttpClient.Builder()
    .build()

class VersionAnalyser(private val version: MinecraftVersionJson) {
    suspend fun analyse(logging: LoggingMessage): List<ResultSection<*>> {
        val clientJar = File(botConfig.dataDir, "client-${version.id}.jar")
        if (!clientJar.exists()) {
            logging.updateMessage("Downloading client-${version.id}.jar...")
            version.downloads?.client?.download(clientJar, OKHTTP)
        }
        val serverJar = File(botConfig.dataDir, "server-${version.id}.jar")
        if (!serverJar.exists()) {
            logging.updateMessage("Downloading server-${version.id}.jar...")
            version.downloads?.server?.download(serverJar, OKHTTP)
        }

        val mergedJar = File(botConfig.dataDir, "merged-${version.id}.jar")
        if (!mergedJar.exists()) {
            logging.updateMessage("Merging JARs...")
            withContext(Dispatchers.IO) {
                val merger = JarMerger(clientJar, serverJar, mergedJar)
                merger.merge()
                merger.close()
            }
        }

        val analysers: List<Analyser<*, *>> = listOf(
                VersionManifestAnalyser(),
                VersionJsonAnalyser(),
                ClassCountAnalyser(),
                FieldSpottingAnalyser()
        )

        analysers.forEach {
            it.resetState()
            logging.updateMessage("Running ${it.getTitle()} against version manifest...")
            it.analyseManifest(version)
        }

        for (phase in Analyser.Phase.values()) {
            for (analyser in analysers) {
                val phases = analyser.getPhases()
                if (!phases.contains(phase)) continue
                logging.updateMessage("Running ${analyser.getTitle()} for phase: $phase...")

                val jar = when (phase) {
                    Analyser.Phase.CLIENT -> clientJar
                    Analyser.Phase.SERVER -> serverJar
                    Analyser.Phase.MERGED -> mergedJar
                    Analyser.Phase.MAPPED -> mergedJar
                }

                try {
                    FileSystems.newFileSystem(jar.toJarURI(), mapOf<String, Any>())
                } catch (e: FileSystemAlreadyExistsException) {
                    FileSystems.getFileSystem(jar.toJarURI())
                }.use { fs ->
                    analyser.analyse(fs, phase)
                }
            }
        }
        return analysers.stream().map { it.completeAnalysis() }.collect(Collectors.toList())
    }
}
