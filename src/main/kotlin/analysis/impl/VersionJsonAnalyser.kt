package me.geek.tom.mcupdateinfo.analysis.impl

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.geek.tom.mcupdateinfo.GSON
import me.geek.tom.mcupdateinfo.LOGGER
import me.geek.tom.mcupdateinfo.analysis.Analyser
import me.geek.tom.mcupdateinfo.analysis.ResourceAnalyser
import me.geek.tom.mcupdateinfo.analysis.ResultSection
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion
import java.nio.file.Files
import java.nio.file.Path

class VersionJsonAnalyser : ResourceAnalyser<VersionJsonAnalyser.VersionJsonResult> {
    private var res: VersionJsonResult? = null

    override fun analyse(item: Path) {
        val obj = GSON.fromJson(Files.newBufferedReader(item), JsonObject::class.java)
        LOGGER.debug("version.json: {}", obj)
        val splitPackVersion = obj["pack_version"].isJsonObject
        val pack_version = if (splitPackVersion) {
            val packObj = obj["pack_version"].asJsonObject
            "Resources: ${packObj["resource"]}\tData: ${packObj["data"]}"
        } else {
            obj["pack_version"].asInt.toString()
        }
        res = VersionJsonResult(
            obj["id"].asString,
            obj["release_target"].asString,
            obj["world_version"].asInt,
            obj["protocol_version"].asInt,
            pack_version,
            obj["build_time"].asString,
            obj["stable"].asBoolean
        )
    }

    override fun resetState() {
        res = null
    }

    override fun completeAnalysis(): ResultSection<VersionJsonResult> {
        return res!!
    }

    override fun shouldAnalyse(path: Path): Boolean {
        return path.fileName.toString() == "version.json"
    }

    override fun getTitle(): String {
        return "version.json analysis"
    }

    override fun getPhase(): Analyser.Phase {
        return Analyser.Phase.CLIENT
    }

    class VersionJsonResult(
        private val id: String,
        private val release_target: String,
        private val world_version: Int,
        private val protocol_version: Int,
        private val pack_version: String,
        private val build_time: String,
        private val stable: Boolean
    ) : ResultSection<VersionJsonResult> {

        override fun toDiscordString(): String {
            return "```" +
                    "Id: $id (${if (stable) "stable" else "unstable"})\n" +
                    "Release target: $release_target\n" +
                    "World version: $world_version\n" +
                    "Protocol version: $protocol_version\n" +
                    "Pack format: $pack_version\n" +
                    "Build time: $build_time\n" +
                    "```"
        }

        override fun getCodec(version: MinecraftVersion): Codec<VersionJsonResult> {
            return CODEC
        }

        companion object {
            val CODEC: Codec<VersionJsonResult> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("id").forGetter { it.id },
                    Codec.STRING.fieldOf("release_target").forGetter { it.release_target },
                    Codec.INT.fieldOf("world_version").forGetter { it.world_version },
                    Codec.INT.fieldOf("protocol_version").forGetter { it.protocol_version },
                    Codec.STRING.fieldOf("pack_version").forGetter { it.pack_version },
                    Codec.STRING.fieldOf("build_time").forGetter { it.build_time },
                    Codec.BOOL.fieldOf("stable").forGetter { it.stable }
                ).apply(instance, ::VersionJsonResult)
            }
        }
        override fun getTitle(): String {
            return "version.json analysis"
        }
    }
}