Puppet: Minecraft ArmorStand Animation Engine
========================
Vanilla Minecraft animation engine for Spigot servers to
render Blender skeletal animations into vanilla .json models
controlled by ArmorStands.

Requirements
------------------------
1. **Kotlin v1.3** - plugin language
2. **Gradle v6.0** - build system
3. **Python (optional)** - for generating some documentation files

Build
------------------------
**Windows**:

Development snapshots:  
`./gradlew.bat build`

Release build:  
`./gradlew.bat release`

The built `.jar` will be located in `build\libs` as either
`puppet-0.0.0-SNAPSHOT.jar` (dev) or `puppet-0.0.0.jar` (release).
The smaller sized files beginning with `phonon-puppet-...` are before
shading in gson and kotlin runtime dependencies. Use these if you want
to separately load dependencies (see dependencies in `build.gradle.kts`).