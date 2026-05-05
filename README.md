# version-dependency-override-force-down

Wave 1, probe #3 of the sbt versioning coverage plan (`docs/SBT_VERSIONING_PLAN.md`).

## Feature exercised

`dependencyOverrides` forces a transitive dependency DOWN to a version older than the version Coursier's highest-wins eviction would naturally select. The override wins even when it pushes the resolved version below what the direct dependency's own POM declares as a requirement.

## Dependencies

| Declared | Artifact (Mend artifactId) | Version | Direct | Notes |
|---|---|---|---|---|
| `"org.typelevel" %% "cats-core" % "2.10.0"` | `cats-core_2.13` | 2.10.0 | yes | Scala `%%` -- suffix `_2.13` appended by Coursier |
| `dependencyOverrides += "org.typelevel" %% "cats-kernel" % "2.9.0"` | `cats-kernel_2.13` | 2.9.0 | no | Override forces transitive DOWN from natural 2.10.0 to 2.9.0 |

Total: 2 packages. Direct: 1. Transitive: 1.

## Expected dependency tree

- `org.typelevel:cats-core_2.13:2.10.0` (Compile, direct, registry, main)
  - `org.typelevel:cats-kernel_2.13:2.9.0` (Compile, transitive, registry, main -- OVERRIDDEN, not 2.10.0)

The critical assertion: `cats-kernel_2.13` must be reported at `2.9.0`, NOT `2.10.0`.

Without `dependencyOverrides`, cats-core 2.10.0 would pull cats-kernel 2.10.0 as a transitive dependency, and Coursier's highest-wins eviction would keep it at 2.10.0. The `dependencyOverrides` setting overrides both eviction and transitive POM requirements -- the resolved version is 2.9.0 even though cats-core 2.10.0 declares a dependency on cats-kernel 2.10.0.

## Mend config

- `.whitesource` pins `sbt: 1.9.8`, `scala: 2.13.12`, `java: 17` via `scanSettings.versioning`. sbt has no dynamic version detection (mend-knowledge `whitesource-config.md` line 148), so the pin is required to keep the probe deterministic across scans. Without this pin, Mend's install-tool may provision a different sbt/Scala/Java combination, which can change the Coursier-resolved tree (different default repositories, different transitive resolution, different artifact suffix at runtime).
- No `whitesource.config` (UA) is needed. Coursier-driven detection from `build.sbt` is sufficient; `runPreStep` is not required for this probe. The `.whitesource` file is the only Mend configuration needed.
- The `sbt` and `scala` values in `.whitesource` match `project/build.properties` (`sbt.version=1.9.8`) and `build.sbt` (`scalaVersion := "2.13.12"`) exactly. A mismatch between these would produce a non-deterministic resolved tree.

## Failure modes exercised

- **Override ignored -- natural resolution reported**: Mend reports `cats-kernel_2.13:2.10.0` instead of `2.9.0`, meaning `dependencyOverrides` was not honored during resolution. This is the primary failure mode this probe is designed to catch.
- **Both versions reported (duplicate)**: Mend reports both `cats-kernel_2.13:2.9.0` and `cats-kernel_2.13:2.10.0`, indicating a failure to merge the overridden and natural resolution paths into a single resolved entry.
- **Transitive missing entirely**: Mend omits `cats-kernel_2.13` from the tree because the 2.9.0 override broke the POM traversal for cats-core 2.10.0.
- **Scala binary suffix stripped**: Mend reports `cats-kernel` instead of `cats-kernel_2.13` -- suffix-stripping regression.
- **Override artifact reported as direct**: Mend classifies `cats-kernel_2.13:2.9.0` as a direct dependency because it appears in `dependencyOverrides` (which is a top-level build setting), rather than correctly classifying it as a transitive brought in by cats-core.

## Probe metadata

```json
{
  "probe_name": "sbt-version-dependency-override-force-down-probe",
  "pattern": "version-dependency-override-force-down",
  "wave": 1,
  "probe_number": 3,
  "pm": "sbt",
  "generated": "2026-05-05",
  "target": "local",
  "sbt_version": "1.9.8",
  "scala_version": "2.13.12",
  "java_version": "17"
}
```