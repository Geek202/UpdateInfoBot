package me.geek.tom.mcupdateinfo.analysis

import io.ktor.util.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.tree.ClassNode
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

interface ClassAnalyser<R : ResultSection<R>> : Analyser<ClassNode, R>, FileVisitor<Path> {
    override fun analyse(fs: FileSystem) {
        for (directory in fs.rootDirectories) {
            Files.walkFileTree(directory, this)
        }
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (file.extension != "class") return FileVisitResult.CONTINUE

        Files.newInputStream(file, StandardOpenOption.READ).use { stream ->
            val reader = ClassReader(stream)
            val classNode = ClassNode(ASM9)
            reader.accept(classNode, 0)
            if (shouldAnalyse(classNode.name))
                analyse(classNode)
        }

        return FileVisitResult.CONTINUE
    }

    override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }
    override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }
    override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }

    fun shouldAnalyse(className: String): Boolean
}
