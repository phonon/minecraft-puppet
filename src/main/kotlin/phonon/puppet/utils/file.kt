/**
 * File utility functions
 */

package phonon.puppet.utils.file

import java.util.stream.Stream
import java.util.stream.Collectors
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.FileVisitResult;
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipFile

// implement toList() for Stream<T>
fun <T: Any> Stream<T>.toList(): List<T> = this.collect(Collectors.toList<T>())

/**
 * Remove file extension from string path name
 */
public fun removeFileExtension(s: String): String {
    val index = s.lastIndexOf(".")
    val withoutExt = if ( index == -1 ) {
        s
    } else {
        s.substring(0, index)
    }

    return withoutExt
}

/**
 * Return list of file names in a directory
 */
public fun listFilesInDir(path: Path, ext: String? = null): List<String> {
    var stream = Files.walk(path, 1)
        .filter { item -> Files.isRegularFile(item) }

    if ( ext !== null ) {
        stream = stream.filter { item -> item.toString().endsWith(ext) }
    }

    val files = stream.toList().map { item -> item.toString() }
    
    return files
}

/**
 * Write input string to output file path
 */
public fun writeStringToFile(s: String, path: Path) {
    Files.newBufferedWriter(
        path,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
    ).use { writer: BufferedWriter ->
        try {
            writer.write(s)
            writer.close()
        }
        catch ( err: Exception ) {
            err.printStackTrace()
        }
    }
}

/**
 * Recursively delete directory
 */
public fun deleteDirectory(path: Path) {
    if ( !Files.exists(path) ) {
        return
    }

    Files.walk(path)
        .sorted(Comparator.reverseOrder())
        .map { p -> p.toFile() }
        .forEach { f -> f.delete() }
}

/**
 * Zips a directory into output path
 */
public fun zipDirectory(folder: Path, outputPath: Path) {
    val zos = ZipOutputStream(FileOutputStream(outputPath.toFile()))
    
    zos.setMethod(ZipOutputStream.DEFLATED)
    zos.setLevel(4)
    
    Files.walkFileTree(folder, object: SimpleFileVisitor<Path>() {
        override public fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            var f = folder.relativize(file).toString()

            // zip must use / separators
            if ( File.separatorChar != '/' ) {
                f = f.replace("\\", "/")
            }
            
            zos.putNextEntry(ZipEntry(f))
            Files.copy(file, zos)
            zos.closeEntry()

            return FileVisitResult.CONTINUE
        }
    });

    zos.finish()
    zos.close()
}