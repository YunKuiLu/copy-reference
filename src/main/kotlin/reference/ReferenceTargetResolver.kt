package reference

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

class ReferenceTargetResolver(
    private val editorResolver: EditorReferenceResolver,
    private val projectViewResolver: ProjectViewReferenceResolver,
) {
    fun resolve(event: AnActionEvent): List<ReferenceTarget> {
        val project = event.project ?: return emptyList()

        val editorTargets = resolveEditor(project, event)
        if (editorTargets.isNotEmpty()) {
            return editorTargets
        }

        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            ?: event.getData(CommonDataKeys.VIRTUAL_FILE)?.let { arrayOf(it) }
            ?: emptyArray()
        return projectViewResolver.resolve(project, files)
    }

    private fun resolveEditor(project: Project, event: AnActionEvent): List<ReferenceTarget> {
        val editor: Editor = event.getData(CommonDataKeys.EDITOR) ?: return emptyList()
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
            ?: PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            ?: return emptyList()
        val clickOffset = editorClickOffset(editor, event)
        return listOfNotNull(editorResolver.resolve(project, editor, psiFile, clickOffset))
    }

    private fun editorClickOffset(editor: Editor, event: AnActionEvent): Int? {
        val mouseEvent = event.inputEvent as? MouseEvent ?: return null
        val editorPoint = SwingUtilities.convertPoint(
            mouseEvent.component,
            mouseEvent.point,
            editor.contentComponent,
        )
        val logicalPosition = editor.xyToLogicalPosition(editorPoint)
        return editor.logicalPositionToOffset(logicalPosition)
    }
}
