package reference

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.MapDataContext
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Point
import javax.swing.JPanel

@Suppress("DEPRECATION")
class ReferenceTargetResolverTest : BasePlatformTestCase() {
    private lateinit var resolver: ReferenceTargetResolver

    override fun setUp() {
        super.setUp()
        val pathService = ReferencePathService()
        resolver = ReferenceTargetResolver(
            editorResolver = EditorReferenceResolver(pathService),
            projectViewResolver = ProjectViewReferenceResolver(pathService),
        )
    }

    fun testCalculatesOffsetFromEditorContentPoint() {
        myFixture.configureByText(
            "Foo.kt",
            """
            fun main() {
                println("hello")
            }
            """.trimIndent(),
        )
        val editor = myFixture.editor
        val position = LogicalPosition(1, 4)
        val point = editor.logicalPositionToXY(position)

        assertEquals(
            editor.logicalPositionToOffset(position),
            resolver.offsetFromEditorMousePoint(editor, point, editor.contentComponent),
        )
    }

    fun testIgnoresMousePointFromUnrelatedComponent() {
        myFixture.configureByText("Foo.kt", "fun main() {}")

        assertNull(
            resolver.offsetFromEditorMousePoint(
                editor = myFixture.editor,
                point = Point(0, 0),
                sourceComponent = JPanel(),
            ),
        )
    }

    fun testHasResolvableContextForEditorAndPsiFileWithoutResolvingTarget() {
        val psiFile = myFixture.configureByText("Foo.kt", "fun main() {}")
        val dataContext = MapDataContext().apply {
            put(CommonDataKeys.PROJECT, project)
            put(CommonDataKeys.EDITOR, myFixture.editor)
            put(CommonDataKeys.PSI_FILE, psiFile)
        }

        assertTrue(resolver.hasResolvableContext(TestActionEvent.createTestEvent(dataContext)))
    }

    fun testHasResolvableContextForVirtualFileWithoutEditor() {
        val virtualFile = myFixture.addFileToProject("Foo.kt", "fun main() {}").virtualFile
        val dataContext = MapDataContext().apply {
            put(CommonDataKeys.PROJECT, project)
            put(CommonDataKeys.VIRTUAL_FILE, virtualFile)
        }

        assertTrue(resolver.hasResolvableContext(TestActionEvent.createTestEvent(dataContext)))
    }

    fun testHasResolvableContextRequiresProject() {
        val dataContext = MapDataContext().apply {
            put(CommonDataKeys.VIRTUAL_FILE, myFixture.addFileToProject("Foo.kt", "fun main() {}").virtualFile)
        }

        assertFalse(resolver.hasResolvableContext(TestActionEvent.createTestEvent(dataContext)))
    }
}
