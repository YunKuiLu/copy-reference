package reference

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferenceFormatterTest {
    private val formatter = ReferenceFormatter()

    @Test
    fun formatsWholeFileReference() {
        val target = ReferenceTarget.File(path = "src/main/kotlin/Foo.kt", lineRange = null)

        assertEquals("@src/main/kotlin/Foo.kt", formatter.format(target))
    }

    @Test
    fun formatsFileRangeReference() {
        val target = ReferenceTarget.File(
            path = "src/main/kotlin/Foo.kt",
            lineRange = LineRange(start = 12, end = 20),
        )

        assertEquals("@src/main/kotlin/Foo.kt#L12-L20", formatter.format(target))
    }

    @Test
    fun formatsSingleLineRangeWithSameStartAndEnd() {
        val target = ReferenceTarget.File(
            path = "src/main/kotlin/Foo.kt",
            lineRange = LineRange(start = 7, end = 7),
        )

        assertEquals("@src/main/kotlin/Foo.kt#L7-L7", formatter.format(target))
    }

    @Test
    fun formatsDirectoryReference() {
        val target = ReferenceTarget.Directory(path = "src/main/kotlin")

        assertEquals("@src/main/kotlin", formatter.format(target))
    }

    @Test
    fun formatsMultipleReferencesOnePerLine() {
        val targets = listOf(
            ReferenceTarget.Directory(path = "src/main/kotlin"),
            ReferenceTarget.File(path = "src/main/kotlin/Foo.kt", lineRange = null),
            ReferenceTarget.File(path = "src/main/kotlin/Bar.kt", lineRange = LineRange(2, 5)),
        )

        assertEquals(
            """
            @src/main/kotlin
            @src/main/kotlin/Foo.kt
            @src/main/kotlin/Bar.kt#L2-L5
            """.trimIndent(),
            formatter.formatAll(targets),
        )
    }
}
