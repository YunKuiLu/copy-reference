package reference

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ReferencePathServiceTest : BasePlatformTestCase() {
    private lateinit var service: ReferencePathService

    override fun setUp() {
        super.setUp()
        service = ReferencePathService()
    }

    fun testUsesProjectRelativePathForProjectFile() {
        val file = myFixture.addFileToProject(
            "src/main/kotlin/Foo.kt",
            "class Foo",
        ).virtualFile

        assertEquals("src/main/kotlin/Foo.kt", service.pathFor(project, file))
    }

    fun testUsesProjectRelativePathForProjectDirectory() {
        myFixture.addFileToProject("src/main/kotlin/Foo.kt", "class Foo")
        val directory = myFixture.findFileInTempDir("src/main/kotlin")

        assertEquals("src/main/kotlin", service.pathFor(project, directory))
    }

    fun testFallsBackToNormalizedAbsolutePathOutsideProject() {
        val outsideFile: VirtualFile = LightVirtualFile(
            "Outside.kt",
            "class Outside",
        )

        assertEquals("Outside.kt", service.pathFor(project, outsideFile))
    }
}
