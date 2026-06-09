package reference

class ReferenceFormatter {
    fun format(target: ReferenceTarget): String {
        return when (target) {
            is ReferenceTarget.Directory -> "@${target.path}"
            is ReferenceTarget.File -> {
                val suffix = target.lineRange?.let { "#L${it.start}-L${it.end}" }.orEmpty()
                "@${target.path}$suffix"
            }
        }
    }

    fun formatAll(targets: List<ReferenceTarget>): String {
        return targets.joinToString(separator = "\n") { format(it) }
    }
}
