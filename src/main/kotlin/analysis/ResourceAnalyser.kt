package me.geek.tom.mcupdateinfo.analysis

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

interface ResourceAnalyser<R : ResultSection<R>> : Analyser<Path, R>, FileVisitor<Path> {
    override fun analyse(fs: FileSystem) {
        for (directory in fs.rootDirectories) {
            Files.walkFileTree(directory, this)
        }
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (file.endsWith(".class") || !shouldAnalyse(file)) return FileVisitResult.CONTINUE
        analyse(file)
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

    fun shouldAnalyse(path: Path): Boolean
}
