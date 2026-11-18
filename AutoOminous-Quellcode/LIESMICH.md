# Auto Ominous Bottle (Fabric, Minecraft 26.3)

Trinkt per Hotkey-Automatik eine Ominous Bottle, sobald weder Bad Omen noch Raid Omen aktiv sind.

## Jar bauen lassen (ohne Installation, ca. 5 Minuten)
1. Auf github.com einloggen -> "New repository" -> Name z.B. `autoominous` -> Create.
2. Dieses Zip entpacken, im neuen Repository "uploading an existing file" waehlen und ALLE Dateien/Ordner
   hochziehen (auch den versteckten Ordner `.github`!) -> "Commit changes".
3. Oben auf den Reiter **Actions** -> der Build "Build Mod" laeuft automatisch (gruener Haken abwarten).
4. Auf den Lauf klicken -> unten bei **Artifacts** `autoominous-mod` herunterladen -> entpacken -> `autoominous-1.0.0.jar`.
5. Jar in den Modrinth-Mods-Ordner ziehen. Zusaetzlich noetig: Fabric Loader 0.19.5+ und Fabric API fuer 26.3, Java 25.

## Benutzung
- Standard-Taste: **K** (aenderbar unter Optionen -> Steuerung -> Auto Ominous Bottle)
- Ominous Bottle muss in der **Hotbar** liegen. Die Mod waehlt sie aus, trinkt, und wechselt zurueck.
- Mit Chat-Meldung AN/AUS.

## Selbst bauen (alternativ)
Java 25 + Gradle 9.6.0 installieren, im Ordner `gradle build` -> Jar in `build/libs/`.
