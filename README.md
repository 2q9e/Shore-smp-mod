# ShoreMod for Fabric

ShoreMod is a client-side Fabric companion for ShoreSMP on Minecraft 1.21.11. It includes support tickets, roster badges, HUD and inventory tools, clip recording, and other server-connected features. This checkout was reconstructed from the supplied ShoreMod 1.1.0 JAR with CFR 0.152. The Java files retain their decompilation notices.

The [Fabric manifest](src/main/resources/fabric.mod.json) records the supported game version, loader, and bundled Rinku dependency.

## Build

Requires Java 21. Run:

```sh
./gradlew build
```

The project uses Fabric Loom 1.14.10 and Intermediary mappings because the recovered bytecode references Minecraft's `class_*` names. The build produces the remapped mod JAR under `build/libs/`.

The embedded Rinku library is kept in `libs/` as the compile and Jar-in-Jar dependency. Its LGPL license and notices are in `licenses/`.
