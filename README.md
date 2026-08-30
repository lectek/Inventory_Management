# Inventory Management (IMS)

Version 1.0.2 — built with Java Swing + SQLite.

Stand-alone desktop app for tracking production, dispatch and stock of
products (name / colour / weight). Packaged as a single executable JAR via
Maven's `assembly` plugin; no external database server required — data is
stored locally in a SQLite file.

## Download (Windows install package)

Download the ready-to-run package — **not** the "Source code" zip — from:

**https://github.com/lectek/Inventory_Management/releases/download/v1.0.2/IMS-1.0.2-win.zip**

It contains `IMS-1.0.2.jar`, `ims_files/` (config + sample DB) and
`INSTALL.txt`. See [Installing on Windows](#installing-on-windows) below.

## Release notes

- Interface with 3 views — Production, Dispatch and Stock.
- Real-time changes in product, colour and weight dropdown.
- Loosely coupled component architecture.
- Production/dispatch datetime column added to the view.
- DB path is no longer hardcoded in `Db.java` — read from `config.properties`.

## Requirements

- **JRE/JDK 8** (32-bit or 64-bit — either works, see note below).
- No other runtime dependency. `sqlite-jdbc` (the SQLite driver) is bundled
  inside the JAR, including its native library for Windows x86 and x86_64,
  Linux and macOS — it is extracted automatically at startup. You do **not**
  need a separate `sqlite3.dll` on the system.

> **32-bit vs 64-bit Java:** the app is plain Java 8 bytecode and does not
> need a large heap, so a 32-bit JRE runs it exactly as well as a 64-bit one
> — the bundled SQLite driver auto-selects the native library matching
> whichever JVM you run. There is no need to install a 64-bit JDK just for
> this app.

## Project layout

```
src/                    Java source (package mysquare.core)
pom.xml                 Maven build (compiles for Java 8, assembles a fat JAR)
ims_files/
  config.properties     Required config file — see "Configuration" below
sample-data/
  rbp-sample.db         Empty SQLite DB (schema only, no rows) for local dev/testing
```

## Building the JAR

```bash
mvn clean package
```

This produces `target/IMS-1.0.2.jar`, a fat JAR with all dependencies
(including the SQLite driver) and `mysquare.core.IMStart` as the main class.

## Configuration — read this carefully

The database path is **hardcoded in the source** at
[`src/mysquare/core/Utility.java`](src/mysquare/core/Utility.java) to:

```
C:/ims_files/config.properties
```

This is not configurable via environment variable or command-line
argument — the file **must** exist at exactly that path on Windows. Its
content:

```properties
DB_PATH=C:/ims_files/rbp.db
DB_DRIVER=jdbc:sqlite:
```

`DB_PATH` can point anywhere on disk you like (it just has to be a valid
SQLite file); only the location of `config.properties` itself is fixed.

## Installing on Windows

1. Confirm Java 8 is installed and on PATH:
   ```cmd
   java -version
   ```
   Any Java 8 build works (32-bit or 64-bit) — see the note above.

2. Create `C:\ims_files\` and place inside it:
   - `config.properties` (from this repo's `ims_files/` folder, or the copy
     shipped in the GitHub Release)
   - `rbp.db` — your real database. **Do not** take this from the public
     GitHub repo/Release: those ship `sample-data/rbp-sample.db`, an empty
     schema-only file, on purpose. Copy your actual working database over
     via a private channel (USB drive, internal file share) — never through
     a public repo. If this is a brand-new install, just rename
     `rbp-sample.db` to `rbp.db` and place it there to start with an empty
     database.

3. Create `C:\ControleEstoque\programa\` and place `IMS-1.0.2.jar` inside.

4. Run it:
   ```cmd
   cd /d C:\ControleEstoque\programa
   java -jar IMS-1.0.2.jar
   ```

5. On first connection the app auto-migrates the `products` table (adds
   `pcode`, `pdesc`, `pprice` columns if missing) — this is expected and
   only happens once per database.

## Security note

`ims_files/rbp.db` in a working installation contains real inventory data
(product names, quantities, sale/production records) and must **never** be
committed to git or uploaded to a public GitHub Release. It is excluded via
`.gitignore`. Only `sample-data/rbp-sample.db` (schema, no rows) is tracked
in this repository.

## Releasing

See the project's release process for how `IMS-1.0.2.jar` +
`ims_files/config.properties` + `sample-data/rbp-sample.db` are packaged
into a downloadable ZIP for installation.
