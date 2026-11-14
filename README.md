# ShoreMod Fabric source

Reconstructed source for ShoreMod Fabric 1.21.11, recovered from the supplied `ShoreMod-Fabric-1.21.11-1.1.0.jar` with CFR 0.152. The Java files are decompiled output, not the original authoring source; CFR notices are retained in each file.

## Build

Requires Java 21. Run:

```sh
./gradlew build
```

The project uses Fabric Loom 1.14.10 and Intermediary mappings because the recovered bytecode references Minecraft's `class_*` names. The build produces the mod JAR under `build/libs/`.

The embedded Rinku library is kept as a compile and Jar-in-Jar dependency. Its LGPL license and notices are in `licenses/`.
