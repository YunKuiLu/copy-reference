package reference

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import java.awt.Component
import java.awt.Point
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

    fun hasResolvableContext(event: AnActionEvent): Boolean {
        if (event.project == null) {
            return false
        }

        val hasEditorContext = event.getData(CommonDataKeys.EDITOR) != null &&
            event.getData(CommonDataKeys.PSI_FILE) != null
        if (hasEditorContext) {
            return true
        }

        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.isNotEmpty() == true ||
            event.getData(CommonDataKeys.VIRTUAL_FILE) != null
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
        val point = event.getData(PlatformDataKeys.CONTEXT_MENU_POINT) ?: return null
        val sourceComponent = event.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) ?: return null
        return offsetFromEditorMousePoint(editor, point, sourceComponent)
    }

    fun offsetFromEditorMousePoint(editor: Editor, point: Point, sourceComponent: Component): Int? {
        if (!editor.ownsMouseComponent(sourceComponent)) {
            return null
        }

        val editorPoint = SwingUtilities.convertPoint(sourceComponent, point, editor.contentComponent)
        val logicalPosition = editor.xyToLogicalPosition(editorPoint)
        return editor.logicalPositionToOffset(logicalPosition)
    }

    private fun Editor.ownsMouseComponent(component: Component): Boolean {
        val gutterComponent = (this as? EditorEx)?.gutterComponentEx as? Component
        return component == contentComponent ||
            component == gutterComponent ||
            SwingUtilities.isDescendingFrom(component, contentComponent) ||
            (gutterComponent != null && SwingUtilities.isDescendingFrom(component, gutterComponent))
    }
}
