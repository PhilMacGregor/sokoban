# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Console Sokoban game in Java 21 (Maven, Lombok, SLF4J/Logback, JNA). Built from an in-house "simple JAR without Spring" archetype; `README-FIRST.TXT` (Czech) describes the archetype rules. Some comments and commit messages are in Czech.

## Commands

Use the Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` elsewhere).

- Build + install distributable: `./mvnw clean install` — copies the jar, one-jar fat jar, javadoc/sources jars, `lib/` dependencies and launcher scripts into `build-application/`.
- Tests: `./mvnw test`; single test: `./mvnw test -Dtest=ClassName#method`.
- SpotBugs runs as part of the build (`check` goal, effort Max, threshold Low, exclusions in `spotbugs-filter.xml`). Suppress individual findings with `@SuppressFBWarnings(value=..., justification=...)`.
- JaCoCo coverage check runs in the build; the threshold is the `<code-coverage>` property in `pom.xml` (currently `0.0`; archetype rule is 1.0 before customer distribution). `**/core/*` is excluded from coverage — per the archetype rules every exclusion must carry a comment explaining why.
- Run: `java -jar build-application/sokoban.jar` (or the one-jar under `build-application/onejar/`, or the copied `sokoban.bat` / `sokoban.sh` launchers).
- Surefire runs tests in parallel at class level (`<parallel>classes</parallel>`) — the pom says not to change this; tests must be safe to run concurrently across classes.

Archetype rules for dependencies: after adding a dependency, build immediately so its license can be checked; runtime dependencies may only use architect-approved licenses. Sections of `pom.xml` marked "DO NOT EDIT" are template-managed.

## Architecture

Two layers: an immutable game core and interchangeable console UIs.

**`core` — pure game logic, immutable**
- `FieldType` enum maps each tile to its map character (`W` wall, `.` floor, ` ` void, `C` crate, `Q` crate on target, `X` target, `P` player, `T` player on target) and an `onTarget` flag used to preserve targets when things move on/off them.
- `BoardState` is an immutable snapshot. It holds `Fields` (a record wrapping `Map<y, Map<x, Field>>`, defensively copied), plus derived crate/target/player positions computed in the constructor. `setField(...)` returns a **new** `BoardState`; nothing is mutated. Coordinates not present in the map read as `VOID`. `isWon()` compares crate positions to target positions. Maps are parsed from strings via `BoardState.fromString` (rows split on CR/LF or `|`; unknown chars throw).
- `SokobanProcessor.play(board, Input)` returns the next `BoardState`. Movement is recursive with a "strength" budget (`PLAYER_STRENGTH = 1` → the player can push exactly one crate); `canMove` validates the chain first, then `move` produces the set of changed fields.
- `Input` enum carries a direction `Point`.

**`ui` — rendering and input loops**
- `ui.cmd.CmdUi` — primary UI: reads arrow keys/ESC directly via the Windows console API (`WindowsInput`, using JNA `Kernel32`), draws with ANSI clear-screen and block-character sprites. Windows-only; throws `IncompatibleUIException` when no Windows console is available (e.g. IDE consoles, non-Windows).
- `ui.debug.DebugUi` — fallback line-based UI (`w/a/s/d` or `up/down/left/right` + Enter) via `Scanner`; implements the `Ui` interface.
- `SokobanMain` tries `CmdUi` and falls back to `DebugUi` on `IncompatibleUIException`. The level is currently a hard-coded text block (`MAP1`).

`maps.txt` contains additional levels in the same character format, separated by blank lines; loading them is not yet implemented.

## Code style

2-space indentation, `final` on parameters and locals, Lombok (`@Getter`, `@RequiredArgsConstructor`, `@Slf4j`, `@Cleanup`) — see `lombok.config` (adds `@Generated` and Eclipse null annotations). PMD/CPD exclusions live in `exclude-pmd.properties` / `exclude-cpd.properties`.
