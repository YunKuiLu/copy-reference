# Copy Reference

Copy Reference is an IntelliJ IDEA plugin that copies AI-agent-friendly references from editor and project tree context menus.

The plugin copies references as plain text. It does not copy source code and does not wrap output in Markdown fences.

## Supported References

- Editor selection: @path/to/File.kt#L12-L20
- Editor right-click inside a method or function: @path/to/File.kt#L12-L20
- Editor right-click inside a class or object: @path/to/File.kt#L1-L40
- Editor fallback for a file: @path/to/File.kt
- Project tree file: @path/to/File.kt
- Project tree directory: @path/to/dir

## Development

- Run tests: ./gradlew test
- Build the plugin: ./gradlew buildPlugin
- Run a sandbox IDE: ./gradlew runIde
