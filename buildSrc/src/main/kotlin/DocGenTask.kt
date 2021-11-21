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

    private class CodeBlockBuilder(
        private val needsImports: () -> Boolean
    ) {
        private enum class CodeType(
            val title: String,
            val style: String
        ) {
            Kotlin("Kotlin", "kotlin"),
            Sql("SQL", "sql")
        }

        private var mode = CodeType.Kotlin
        private val code = linkedMapOf<CodeType, StringBuilder>()

        fun addLine(line: String) {
            if (line.contains("assertGeneratedSql(\"\"\"")) {
                mode = CodeType.Sql
            } else if (mode == CodeType.Sql && line.contains("\"\"\")")) {
                mode = CodeType.Kotlin
            } else {
                val builder = code[mode]

                if (builder != null) {
                    builder.append("\n")
                    builder.append(line)
                } else {
                    code[mode] = StringBuilder(line)
                }
            }
        }

        fun outputTo(output: ArrayList<String>) {
            with (output) {
                fun outputCode(type: CodeType, content: String) {
                    add("```${type.style}")
                    add(content.trimIndent().trim())
                    add("```")
                }

                if (code.size > 1) {
                    add("````mdx-code-block")
                    if (needsImports()) {
                        add("import Tabs from '@theme/Tabs';")
                        add("import TabItem from '@theme/TabItem';")
                    }
                    add("")
                    add("<Tabs>")
                    code.forEach { (type, content) ->
                        add("""<TabItem value="${type.style}" label="${type.title}">""")
                        add("")
                        outputCode(type, "$content")
                        add("")
                        add("</TabItem>")
                    }
                    add("</Tabs>")
                    add("````")
                } else {
                    code.forEach { (type, content) ->
                        outputCode(type, "$content")
                    }
                }
            }
        }
    }

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

        var needsImports = true
        var codeBlock: CodeBlockBuilder? = null

        texts
            .asSequence()
            .mapNotNull {
                when (val trimmed = "${it.second}".trimIndent().trim()) {
                    "SHOW" -> { show = true; null }
                    "HIDE" -> { show = false; null }
                    "" -> null
                    else -> if (show) {
                        it.first to trimmed
                    } else {
                        null
                    }
                }
            }
            .forEach { (isCode, it) ->
                if (codeBlock != null && !isCode) {
                    codeBlock?.outputTo(output)
                    codeBlock = null
                } else if (codeBlock == null && isCode) {
                    codeBlock = CodeBlockBuilder {
                        if (needsImports) {
                            needsImports = false
                            true
                        } else {
                            false
                        }
                    }
                } else if (isCode) {
                    codeBlock?.addLine("")
                }

                if (isCode) {
                    it.splitToSequence('\n').forEach {
                        codeBlock?.addLine(it)
                    }
                } else {
                    output.add(it)
                }
            }

        codeBlock?.outputTo(output)

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