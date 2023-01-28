package io.koalaql.docs

import io.koalaql.kapshot.CapturedBlock
import io.koalaql.markout.MarkoutDsl
import io.koalaql.markout.md.Markdown
import io.koalaql.markout.md.markdownString

interface TabBuilder {
    fun tab(label: String, lang: String, block: String)

    fun kotlin(block: String) = tab("Kotlin", "kotlin", block)
    fun sql(block: String) = tab("SQL", "sql", block)
}

class TabbedBlocks {
    private var needsImports = true

    context(Markdown)
    fun tabs(builder: TabBuilder.() -> Unit) {
        val tabs = arrayListOf<String>()

        object : TabBuilder {
            override fun tab(label: String, lang: String, block: String) {
                tabs.add("""
<TabItem value="$lang" label="$label">

${markdownString { code(lang, block) }}

</TabItem>
            """.trim())
            }
        }.builder()

        val imports = if (needsImports) {
            """
                import Tabs from '@theme/Tabs';
                import TabItem from '@theme/TabItem';
            """.trimIndent()
        } else {
            ""
        }

        needsImports = false

        code("mdx-code-block", """
$imports

<Tabs>
${tabs.joinToString("\n")}
</Tabs>
    """.trim())
    }

    context(Markdown, ExampleData)
    fun withGeneratedSql(block: CapturedBlock<Unit>) {
        val kotlin = execBlock(block)

        tabs {
            kotlin(kotlin)
            sql(popGenerated())
        }
    }
}