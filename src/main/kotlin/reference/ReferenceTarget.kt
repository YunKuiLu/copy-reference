package reference

data class LineRange(val start: Int, val end: Int) {
    init {
        require(start >= 1) { "start line must be 1 or greater" }
        require(end >= start) { "end line must be greater than or equal to start line" }
    }
}

sealed interface ReferenceTarget {
    val path: String

    data class File(
        override val path: String,
        val lineRange: LineRange? = null,
    ) : ReferenceTarget

    data class Directory(
        override val path: String,
    ) : ReferenceTarget
}
