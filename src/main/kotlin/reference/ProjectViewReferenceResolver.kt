package reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ProjectViewReferenceResolver(
    private val pathService: ReferencePathService,
) {
    fun resolve(project: Project, files: Array<VirtualFile>): List<ReferenceTarget> {
        return files.mapNotNull { file ->
            val path = pathService.pathFor(project, file) ?: return@mapNotNull null
            if (file.isDirectory) {
                ReferenceTarget.Directory(path)
            } else {
                ReferenceTarget.File(path)
            }
        }
    }
}
