import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

abstract class DocGenTask : DefaultTask() {
    private val oldPrefix = "${project.projectDir.absolutePath}/src/test/kotlin/io/koalaql/docs"

    private fun File.outputPath(): String {
        val newPrefix = "${project.projectDir.absolutePath}/site/docs"

        check (absolutePath.startsWith(oldPrefix))

        return "$newPrefix${absolutePath.removePrefix(oldPrefix)}"
    }

    private val NUL = 0.toChar()

    private fun transformToMd(source: String): String {
        /* this low quality approach does the trick for now */

        val texts = arrayListOf(Pair(true, StringBuilder()))

        var commentDepth = 0
        var last = NUL

        source.forEach { ch ->
            when {
                last == '/' && ch == '*' -> {
                    if (commentDepth == 0) {
                        texts.add(Pair(false, StringBuilder()))
                    }
                    commentDepth++
                    last = NUL
                }
                last == '*' && ch == '/' -> {
                    commentDepth--
                    if (commentDepth == 0) {
                        texts.add(Pair(true, StringBuilder()))
                    }
                    last = NUL
                }
                else -> {
                    if (last != NUL) {
                        texts.last().second.append(last)
                    }

                    last = ch
                }
            }
        }

        var show = false

        val output = arrayListOf<String>()

        var wasCode = false

        texts
            .asSequence()
            .mapNotNull {
                when (val trimmed = "${it.second}".trimIndent().trim()) {
                    "SHOW" -> { show = true; null }
                    "HIDE" -> { show = false; null }
                    "" -> null
                    else -> if (show) {
                        /*if (it.first) {
                            it.first to "```kotlin\n$trimmed\n```"
                        } else {
                            it.first to trimmed
                        }*/

                        it.first to trimmed
                    } else {
                        null
                    }
                }
            }
            .forEach { (isCode, it) ->
                if (wasCode && !isCode) {
                    output.add("```")
                } else if (isCode && !wasCode) {
                    output.add("```kotlin")
                } else if (isCode) {
                    output.add("")
                }

                output.add(it)

                wasCode = isCode
            }

        if (wasCode) output.add("```")

        return output.joinToString("\n")
    }

    private fun visitDirectory(source: File, dest: File) {
        dest.mkdirs()

        source.listFiles().orEmpty().forEach { file ->
            val output = file.outputPath()

            if (file.isDirectory) {
                visitDirectory(file, File(output))
            } else {
                if (output.endsWith(".kt")) {
                    Files.writeString(
                        Path.of("${output.removeSuffix(".kt")}.md"),
                        transformToMd(file.readText()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                } else {
                    Files.copy(file.toPath(), Path.of(output), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    @TaskAction
    fun copyTree() {
        val root = File(oldPrefix)
        val output = File(root.outputPath())

        output.deleteRecursively()

        visitDirectory(root, output)
    }
}