package reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.WindowManager
import io.github.yunkuilu.MyMessageBundle

class ReferenceFeedback {
    fun copied(project: Project, formattedText: String, count: Int) {
        val message = if (count == 1) {
            MyMessageBundle.message("feedback.copyReference.copied.single", formattedText)
        } else {
            MyMessageBundle.message("feedback.copyReference.copied.multiple", count)
        }
        show(project, message)
    }

    fun failed(project: Project?) {
        if (project != null) {
            show(project, MyMessageBundle.message("feedback.copyReference.failed"))
        }
    }

    private fun show(project: Project, message: String) {
        val statusBar: StatusBar? = WindowManager.getInstance().getStatusBar(project)
        statusBar?.info = message
    }
}
