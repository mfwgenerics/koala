package mfwgenerics.kotq.jdbc

enum class CaseMode {
    UPPERCASE {
        override fun applyTo(string: String): String = string.uppercase()
    },
    LOWERCASE {
        override fun applyTo(string: String): String = string.lowercase()
    },
    PRESERVE {
        override fun applyTo(string: String): String = string
    };

    abstract fun applyTo(string: String): String
}