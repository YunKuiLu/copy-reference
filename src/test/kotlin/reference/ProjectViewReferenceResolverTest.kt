package reference

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files

class ProjectViewReferenceResolverTest : BasePlatformTestCase() {
    private lateinit var resolver: ProjectViewReferenceResolver

    override fun setUp() {
        super.setUp()
        resolver = ProjectViewReferenceResolver(ReferencePathService())
    }

    fun testResolvesFileAsWholeFileReference() {
        val file = createProjectFile("src/main/kotlin/Foo.kt", "class Foo")

        assertEquals(
            listOf(ReferenceTarget.File("src/main/kotlin/Foo.kt")),
            resolver.resolve(project, arrayOf(file)),
        )
    }

    fun testResolvesDirectoryAsDirectoryReference() {
        createProjectFile("src/main/kotlin/Foo.kt", "class Foo")
        val directory = findProjectFile("src/main/kotlin")

        assertEquals(
            listOf(ReferenceTarget.Directory("src/main/kotlin")),
            resolver.resolve(project, arrayOf(directory)),
        )
    }

    fun testResolvesMultipleTargetsInSelectionOrder() {
        val file = createProjectFile("src/main/kotlin/Foo.kt", "class Foo")
        val directory = findProjectFile("src/main/kotlin")

        assertEquals(
            listOf(
                ReferenceTarget.Directory("src/main/kotlin"),
                ReferenceTarget.File("src/main/kotlin/Foo.kt"),
            ),
            resolver.resolve(project, arrayOf(directory, file)),
        )
    }

    private fun createProjectFile(relativePath: String, content: String): VirtualFile {
        val path = projectBasePath().resolve(relativePath)
        Files.createDirectories(path.parent)
        Files.writeString(path, content)
        return findProjectFile(relativePath)
    }

    private fun findProjectFile(relativePath: String): VirtualFile {
        val path = projectBasePath().resolve(relativePath)
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
            ?: error("Expected project file to be visible to LocalFileSystem")
    }

    private fun projectBasePath() = project.basePath
        ?.let { java.nio.file.Path.of(it) }
        ?: error("Expected test project to have a basePath")
}
