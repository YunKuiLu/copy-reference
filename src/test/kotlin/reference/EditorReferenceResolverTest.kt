package reference

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path

class EditorReferenceResolverTest : BasePlatformTestCase() {
    private lateinit var resolver: EditorReferenceResolver

    override fun setUp() {
        super.setUp()
        resolver = EditorReferenceResolver(ReferencePathService())
    }

    fun testSelectionUsesCoveredOneBasedLineRange() {
        val psiFile = createProjectPsiFile(
            "Foo.kt",
            """
            class Foo {
                fun one() = 1
                fun two() = 2
            }
            """.trimIndent(),
        )
        withEditor(psiFile) { editor ->
            val document = editor.document
            val start = document.getLineStartOffset(1) + 4
            val end = document.getLineEndOffset(2) - 1
            editor.selectionModel.setSelection(start, end)

            assertEquals(
                ReferenceTarget.File("Foo.kt", LineRange(2, 3)),
                resolver.resolve(project, editor, psiFile, clickOffset = start),
            )
        }
    }

    fun testWholeFileFallbackWhenNoSelectionAndNoDeclarationAtClick() {
        val psiFile = createProjectPsiFile(
            "notes.txt",
            """
            plain text
            with no psi declaration
            """.trimIndent(),
        )

        withEditor(psiFile) { editor ->
            assertEquals(
                ReferenceTarget.File("notes.txt"),
                resolver.resolve(project, editor, psiFile, clickOffset = 0),
            )
        }
    }

    fun testNoSelectionInsideJavaMethodUsesMethodRange() {
        myFixture.configureByText(
            "Foo.java",
            """
            class Foo {
                void first() {
                    System.out.println("first");
                }

                void second() {
                    System.out.println("second");
                }
            }
            """.trimIndent(),
        )
        val editor = myFixture.editor
        val clickOffset = editor.document.text.indexOf("println(\"first\")")

        assertEquals(
            ReferenceTarget.File("Foo.java", LineRange(2, 4)),
            resolver.resolve(project, editor, myFixture.file, clickOffset),
        )
    }

    fun testNoSelectionInsideJavaClassUsesClassRange() {
        myFixture.configureByText(
            "Foo.java",
            """
            class Foo {
                int value;
            }
            """.trimIndent(),
        )
        val editor = myFixture.editor
        val clickOffset = editor.document.text.indexOf("value")

        assertEquals(
            ReferenceTarget.File("Foo.java", LineRange(1, 3)),
            resolver.resolve(project, editor, myFixture.file, clickOffset),
        )
    }

    fun testNoSelectionInsideKotlinFunctionUsesFunctionRange() {
        myFixture.configureByText(
            "Foo.kt",
            """
            class Foo {
                fun first() {
                    println("first")
                }
            }
            """.trimIndent(),
        )
        val editor = myFixture.editor
        val clickOffset = editor.document.text.indexOf("println(\"first\")")

        assertEquals(
            ReferenceTarget.File("Foo.kt", LineRange(2, 4)),
            resolver.resolve(project, editor, myFixture.file, clickOffset),
        )
    }

    fun testNoSelectionInsideKotlinClassUsesClassRange() {
        myFixture.configureByText(
            "Foo.kt",
            """
            class Foo {
                val value = 1
            }
            """.trimIndent(),
        )
        val editor = myFixture.editor
        val clickOffset = editor.document.text.indexOf("value")

        assertEquals(
            ReferenceTarget.File("Foo.kt", LineRange(1, 3)),
            resolver.resolve(project, editor, myFixture.file, clickOffset),
        )
    }

    private fun createProjectPsiFile(relativePath: String, content: String): PsiFile {
        val path = projectBasePath().resolve(relativePath)
        Files.createDirectories(path.parent ?: projectBasePath())
        Files.writeString(path, content)
        val virtualFile = refreshProjectFile(path)
        return PsiManager.getInstance(project).findFile(virtualFile)
            ?: error("Expected PSI file for $relativePath")
    }

    private fun withEditor(psiFile: PsiFile, block: (Editor) -> Unit) {
        val virtualFile = psiFile.virtualFile ?: error("Expected virtual file")
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
            ?: error("Expected document for ${virtualFile.path}")
        val editor = EditorFactory.getInstance().createEditor(document, project, virtualFile, false)
        try {
            block(editor)
        } finally {
            EditorFactory.getInstance().releaseEditor(editor)
        }
    }

    private fun refreshProjectFile(path: Path): VirtualFile {
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
            ?: error("Expected project file to be visible to LocalFileSystem")
    }

    private fun projectBasePath() = project.basePath
        ?.let { Path.of(it) }
        ?: error("Expected test project to have a basePath")
}
