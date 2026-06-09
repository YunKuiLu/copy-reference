package reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ReferencePathService {
    fun pathFor(project: Project, file: VirtualFile): String? {
        val filePath = file.path.normalizePath()
        val basePath = project.basePath?.normalizePath()
        val lightProjectPaths = listOf(
            "/${project.name.normalizePath()}",
            "/src",
        ).distinct()

        if (filePath == basePath) {
            return file.name.normalizePath().takeIf { it.isNotBlank() }
        }

        if (basePath != null && filePath.startsWith("$basePath/")) {
            return filePath.removePrefix("$basePath/").takeIf { it.isNotBlank() }
        }

        if (file.url.startsWith("temp://")) {
            val lightRelativePath = lightProjectPaths
                .firstNotNullOfOrNull { lightProjectPath ->
                    filePath.removeProjectPrefix(lightProjectPath)
                }

            if (lightRelativePath != null) {
                return lightRelativePath
            }
        }

        return filePath.normalizeFallbackPath().takeIf { it.isNotBlank() }
    }

    private fun String.normalizePath(): String = replace('\\', '/').trimEnd('/')

    private fun String.normalizeFallbackPath(): String {
        return if (startsWith('/') && indexOf('/', startIndex = 1) == -1) {
            removePrefix("/")
        } else {
            this
        }
    }

    private fun String.removeProjectPrefix(projectPath: String): String? {
        return when {
            this == projectPath -> substringAfterLast('/').takeIf { it.isNotBlank() }
            startsWith("$projectPath/") -> removePrefix("$projectPath/").takeIf { it.isNotBlank() }
            else -> null
        }
    }
}
