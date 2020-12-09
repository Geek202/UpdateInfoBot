package me.geek.tom.mcupdateinfo.analysis.impl

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.geek.tom.mcupdateinfo.analysis.Analyser
import me.geek.tom.mcupdateinfo.analysis.ClassAnalyser
import me.geek.tom.mcupdateinfo.analysis.ResultSection
import me.geek.tom.mcupdateinfo.ext.MinecraftVersion
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class ClassCountAnalyser : ClassAnalyser<ClassCountAnalyser.ClassCountResults> {

    private var total = 0 //
    private var classes = 0
    private var abstractClasses = 0
    private var finalClasses = 0
    private var interfaces = 0
    private var innerClasses = 0 //
    private var enums = 0 //

    override fun analyse(item: ClassNode) {
        total++
        if (item.outerClass != null) {
            innerClasses++
        }
        when {
            item.superName == "java/lang/Enum" -> {
                enums++
            }
            item.access and Opcodes.ACC_INTERFACE != 0 -> {
                interfaces++
            }
            else -> {
                classes++
            }
        }
        if (item.access and Opcodes.ACC_FINAL != 0) {
            finalClasses++
        }
        if (item.access and Opcodes.ACC_ABSTRACT != 0) {
            abstractClasses++
        }
    }

    override fun shouldAnalyse(className: String): Boolean {
        return true
    }

    override fun completeAnalysis(): ResultSection<ClassCountResults> {
        return ClassCountResults(
            total,
            classes,
            abstractClasses,
            finalClasses,
            interfaces,
            innerClasses,
            enums
        )
    }

    override fun resetState() {
        total = 0
        classes = 0
        abstractClasses = 0
        finalClasses = 0
        interfaces = 0
        innerClasses = 0
        enums = 0
    }

    override fun getTitle(): String {
        return "Basic class analysis"
    }

    override fun getPhase(): Analyser.Phase {
        return Analyser.Phase.MERGED
    }

    class ClassCountResults(
        private val total: Int,
        private val classes: Int,
        private val abstractClasses: Int,
        private val finalClasses: Int,
        private val interfaces: Int,
        private val innerClasses: Int,
        private val enums: Int
    ) : ResultSection<ClassCountResults> {
        override fun toDiscordString(): String {
            return "```\n" +
                    "Total classes/interfaces: $total\n" +
                    "Classes: $classes\n" +
                    "Abstract classes: $abstractClasses\n" +
                    "Final classes: $finalClasses\n" +
                    "Inner classes: $innerClasses\n" +
                    "Interfaces: $interfaces\n" +
                    "Enums: $enums\n" +
                    "```"
        }

        override fun getCodec(version: MinecraftVersion): Codec<ClassCountResults> {
            return CODEC
        }

        override fun getTitle(): String {
            return "Basic class analysis"
        }

        companion object {
            private val CODEC: Codec<ClassCountResults> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.INT.fieldOf("total").forGetter { it.total },
                    Codec.INT.fieldOf("classes").forGetter { it.classes },
                    Codec.INT.fieldOf("abstract_classes").forGetter { it.abstractClasses },
                    Codec.INT.fieldOf("final_classes").forGetter { it.finalClasses },
                    Codec.INT.fieldOf("interfaces").forGetter { it.interfaces },
                    Codec.INT.fieldOf("inner_classes").forGetter { it.innerClasses },
                    Codec.INT.fieldOf("enums").forGetter { it.enums }
                ).apply(instance, ::ClassCountResults)
            }
        }
    }
}