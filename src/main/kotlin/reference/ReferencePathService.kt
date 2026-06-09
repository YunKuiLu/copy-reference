package reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ReferencePathService {
    @Suppress("DEPRECATION")
    fun pathFor(project: Project, file: VirtualFile): String? {
        val filePath = file.path.normalizePath()
        val projectRoots = listOfNotNull(
            project.basePath?.normalizePath(),
            project.baseDir?.path?.normalizePath(),
        ).distinct()

        projectRoots.firstNotNullOfOrNull { projectRoot ->
            filePath.removeProjectPrefix(projectRoot)
        }?.let { relativePath ->
            return relativePath
        }

        return filePath.takeIf { it.isNotBlank() }
    }

    private fun String.normalizePath(): String = replace('\\', '/').trimEnd('/')

    private fun String.removeProjectPrefix(projectPath: String): String? {
        return when {
            this == projectPath -> substringAfterLast('/').takeIf { it.isNotBlank() }
            startsWith("$projectPath/") -> removePrefix("$projectPath/").takeIf { it.isNotBlank() }
            else -> null
        }
    }
}
