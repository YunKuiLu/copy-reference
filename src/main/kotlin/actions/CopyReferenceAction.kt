package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import reference.ClipboardService
import reference.EditorReferenceResolver
import reference.ProjectViewReferenceResolver
import reference.ReferenceFeedback
import reference.ReferenceFormatter
import reference.ReferencePathService
import reference.ReferenceTargetResolver

class CopyReferenceAction : AnAction() {
    private val pathService = ReferencePathService()
    private val formatter = ReferenceFormatter()
    private val clipboardService = ClipboardService()
    private val feedback = ReferenceFeedback()
    private val resolver = ReferenceTargetResolver(
        editorResolver = EditorReferenceResolver(pathService),
        projectViewResolver = ProjectViewReferenceResolver(pathService),
    )

    override fun update(event: AnActionEvent) {
        val targets = resolver.resolve(event)
        event.presentation.isEnabledAndVisible = targets.isNotEmpty()
    }

    override fun actionPerformed(event: AnActionEvent) {
        val targets = resolver.resolve(event)
        if (targets.isEmpty()) {
            feedback.failed(event.project)
            return
        }

        val text = formatter.formatAll(targets)
        clipboardService.copy(text)
        event.project?.let { feedback.copied(it, text, targets.size) }
    }
}
