package me.geek.tom.mcupdateinfo.analysis.impl

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.geek.tom.mcupdateinfo.analysis.Analyser
import me.geek.tom.mcupdateinfo.analysis.ClassAnalyser
import me.geek.tom.mcupdateinfo.analysis.ResultSection
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion
import org.objectweb.asm.tree.ClassNode

class FieldSpottingAnalyser : ClassAnalyser<FieldSpottingAnalyser.FieldSpottingResults> {

    private var codecs = 0

    override fun analyse(item: ClassNode) {
        for (field in item.fields) {
            if (field.desc == "Lcom/mojang/serialization/Codec;") {
                codecs++
            }
        }
    }

    override fun shouldAnalyse(className: String): Boolean {
        return true
    }

    override fun completeAnalysis(): ResultSection<FieldSpottingResults> {
        return FieldSpottingResults(codecs)
    }

    override fun resetState() {
        codecs = 0
    }

    override fun getTitle(): String {
        return "Field spotting analysis"
    }

    override fun getPhase(): Analyser.Phase {
        return Analyser.Phase.MERGED
    }

    class FieldSpottingResults(
        private val codecs: Int
    ) : ResultSection<FieldSpottingResults> {
        override fun toDiscordString(): String {
            return "```\n" +
                    "Codec fields: $codecs\n" +
                    "```"
        }

        override fun getCodec(version: MinecraftVersion): Codec<FieldSpottingResults> {
            return CODEC
        }

        override fun getTitle(): String {
            return "Field spotting analysis"
        }

        companion object {
            val CODEC: Codec<FieldSpottingResults> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.INT.fieldOf("codecs").forGetter { it.codecs }
                ).apply(instance, ::FieldSpottingResults)
            }
        }
    }
}