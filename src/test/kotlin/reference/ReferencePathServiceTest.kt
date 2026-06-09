package reference

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files

class ReferencePathServiceTest : BasePlatformTestCase() {
    private lateinit var service: ReferencePathService

    override fun setUp() {
        super.setUp()
        service = ReferencePathService()
    }

    fun testUsesProjectRelativePathForProjectFile() {
        val file = createProjectFile("src/main/kotlin/Foo.kt", "class Foo")

        assertEquals("src/main/kotlin/Foo.kt", service.pathFor(project, file))
    }

    fun testUsesProjectRelativePathForProjectDirectory() {
        createProjectFile("src/main/kotlin/Foo.kt", "class Foo")
        val directoryPath = projectBasePath().resolve("src/main/kotlin")
        val directory = LocalFileSystem.getInstance()
            .refreshAndFindFileByNioFile(directoryPath)
            ?: error("Expected project directory to be visible to LocalFileSystem")

        assertEquals("src/main/kotlin", service.pathFor(project, directory))
    }

    fun testFallsBackToNormalizedAbsolutePathOutsideProject() {
        val outsidePath = Files.createTempFile("copy-reference-outside-", ".kt")
        try {
            Files.writeString(outsidePath, "class Outside")
            val outsideFile: VirtualFile = LocalFileSystem.getInstance()
                .refreshAndFindFileByNioFile(outsidePath)
                ?: error("Expected temp file to be visible to LocalFileSystem")
            val expectedPath = outsidePath.toAbsolutePath().toString()
                .replace('\\', '/')
                .trimEnd('/')

            assertEquals(expectedPath, service.pathFor(project, outsideFile))
        } finally {
            Files.deleteIfExists(outsidePath)
        }
    }

    private fun createProjectFile(relativePath: String, content: String): VirtualFile {
        val path = projectBasePath().resolve(relativePath)
        Files.createDirectories(path.parent)
        Files.writeString(path, content)
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
            ?: error("Expected project file to be visible to LocalFileSystem")
    }

    private fun projectBasePath() = project.basePath
        ?.let { java.nio.file.Path.of(it) }
        ?: error("Expected test project to have a basePath")
}
