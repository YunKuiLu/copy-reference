package reference

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class EditorReferenceResolver(
    private val pathService: ReferencePathService,
) {
    fun resolve(project: Project, editor: Editor, psiFile: PsiFile, clickOffset: Int?): ReferenceTarget.File? {
        val virtualFile = psiFile.virtualFile ?: return null
        val path = pathService.pathFor(project, virtualFile) ?: return null

        if (editor.selectionModel.hasSelection()) {
            return ReferenceTarget.File(
                path = path,
                lineRange = selectedLineRange(
                    document = editor.document,
                    selectionStart = editor.selectionModel.selectionStart,
                    selectionEnd = editor.selectionModel.selectionEnd,
                ),
            )
        }

        val offset = clickOffset?.coerceIn(0, editor.document.textLength)
            ?: editor.caretModel.offset.coerceIn(0, editor.document.textLength)
        val declarationRange = declarationLineRangeAt(psiFile, editor.document, offset)
        return ReferenceTarget.File(path, declarationRange)
    }

    private fun selectedLineRange(document: Document, selectionStart: Int, selectionEnd: Int): LineRange {
        val boundedStart = selectionStart.coerceIn(0, document.textLength)
        val boundedEndExclusive = selectionEnd.coerceIn(0, document.textLength)
        val lastSelectedOffset = (boundedEndExclusive - 1).coerceAtLeast(boundedStart)

        return LineRange(
            start = document.getLineNumber(boundedStart) + 1,
            end = document.getLineNumber(lastSelectedOffset) + 1,
        )
    }

    private fun declarationLineRangeAt(psiFile: PsiFile, document: Document, offset: Int): LineRange? {
        val element = psiFile.findElementAt(offset) ?: return null
        val declaration = findEnclosingDeclaration(element) ?: return null
        return declaration.toLineRange(document)
    }

    private fun findEnclosingDeclaration(element: PsiElement): PsiElement? {
        return null
    }

    private fun PsiElement.toLineRange(document: Document): LineRange {
        val startOffset = textRange.startOffset.coerceIn(0, document.textLength)
        val endOffset = (textRange.endOffset - 1).coerceIn(startOffset, document.textLength)

        return LineRange(
            start = document.getLineNumber(startOffset) + 1,
            end = document.getLineNumber(endOffset) + 1,
        )
    }
}
