package me.geek.tom.mcupdateinfo.analysis.impl

import com.mojang.serialization.Codec
import me.geek.tom.mcdiffer.json.MinecraftVersionJson
import me.geek.tom.mcupdateinfo.analysis.Analyser
import me.geek.tom.mcupdateinfo.analysis.ResultSection
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion

class VersionManifestAnalyser : Analyser<Nothing, VersionManifestAnalyser.Results> {

    private var downloads: MinecraftVersionJson.Downloads? = null

    override fun analyseManifest(manifest: MinecraftVersionJson) {
        downloads = manifest.downloads
    }

    override fun completeAnalysis(): ResultSection<Results> {
        return Results(downloads!!)
    }

    override fun resetState() {
        downloads = null
    }

    override fun getTitle(): String {
        return "Version manifest analysis"
    }

    override fun getPhases(): Set<Analyser.Phase> {
        return emptySet()
    }

    class Results(
            private val downloads: MinecraftVersionJson.Downloads
    ) : ResultSection<Results> {
        override fun toDiscordString(): String {
            return """
                |```yml
                |Client:
                |   Url: ${downloads.client?.url}
                |   Size: ${downloads.client?.size}
                |   SHA1: ${downloads.client?.sha1}
                |   Mappings:
                |       Url: ${downloads.cMappings?.url}
                |       Size: ${downloads.cMappings?.size}
                |       SHA1: ${downloads.cMappings?.sha1}
                |Server:
                |   Url: ${downloads.server?.url}
                |   Size: ${downloads.server?.size}
                |   SHA1: ${downloads.server?.sha1}
                |   Mappings:
                |       Url: ${downloads.sMappings?.url}
                |       Size: ${downloads.sMappings?.size}
                |       SHA1: ${downloads.sMappings?.sha1}
                |```
            """.trimMargin()
        }

        override fun getCodec(version: MinecraftVersion): Codec<Results> {
            return Codec.unit(null)
        }

        override fun getTitle(): String {
            return "Version manifest analysis"
        }
    }
}