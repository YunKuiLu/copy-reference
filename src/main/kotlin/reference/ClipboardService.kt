package reference

import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

class ClipboardService {
    fun copy(text: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
}
