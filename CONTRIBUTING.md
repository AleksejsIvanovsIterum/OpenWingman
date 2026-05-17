# Contributing to OpenWingman

Thanks for your interest. This is a small, focused project — we welcome
patches that move it forward, and we're strict about a few things.

---

## Two firm rules

1. **No proprietary code.** Do not paste decompiled bytecode, source from
   `Wingman.apk`, or assets from any Sound Devices software into this
   repository. If you have read those for understanding, that's fine —
   write your contribution from `docs/PROTOCOL.md`, not from their code.
   This is the **clean-room** boundary that protects everyone.

2. **No trademarks in code or UI.** Refer to recorders by generic terms
   in user-facing UI. Use "Sound Devices" or model names ("833",
   "MixPre-6 II", "Scorpio") only in factual descriptions of
   compatibility — never as branding.

---

## Setup

Requirements:
- Android Studio Ladybug+ (or any IDE with Kotlin 2.0 support)
- JDK 17
- Android SDK 34

```bash
git clone https://github.com/AleksejsIvanovsIterum/OpenWingman.git
cd OpenWingman
./gradlew assembleDebug
```

The CI workflow (`.github/workflows/build.yml`) is the source of truth
for the supported toolchain.

---

## Architecture

Multi-module Gradle:

```
:app                   Entry, MainActivity, ConnectionViewModel
:core-ui               Theme, icons, primitives, meters, frame
:core-protocol         Pure Kotlin: CLink framing, checksum, auth, typed commands
:core-transport        Transport interface (no Android deps)
:transport-ble         Nordic BLE adapter
:domain                Session FSM
:feature-transport     Live transport screen + ViewModels
:feature-takes         Take list + editor
:feature-reports       Sound reports
:feature-device        Device info / settings
:feature-scan          Device picker
```

Dependency direction: features depend on `:core-ui` / `:domain` /
`:core-protocol`. Nothing in `:core-*` may depend on a feature.

---

## Coding conventions

- **Kotlin official style.** Detekt rules are in flight; for now lean on
  Android Studio's default formatter.
- **Composable previews** for every UI component, light + dark.
- **Tests on the protocol.** Anything touching `core-protocol` ships
  with JUnit5 tests. Other modules: tests welcome but not required.
- **One feature per PR.** Small, reviewable changes.
- **No telemetry**, no analytics, no crash reporters. Ever.

---

## Adding a new CLink command

1. Add the numeric code to `CommandId.kt` (preserve numeric order).
2. Create a typed `CLinkRequest` in `core-protocol/commands/` next to a
   logically related group (Transport / DeviceInfo / Settings / etc.).
3. If the response has structure, add a parser companion that returns
   a `CLinkResponse` subtype.
4. Update `docs/PROTOCOL.md` §3 (the command table) and §5 (payload
   schemas) accordingly.
5. Add at least one round-trip test in `core-protocol/src/test/`.

---

## Reporting bugs

For protocol-level bugs (wrong checksum, mis-decoded field):
1. Capture the raw frame as hex (`Frame.toByteArray().joinToString { "%02x".format(it) }`).
2. Note your firmware version (Device tab).
3. Open an issue with both, plus what you expected.

For UI bugs: a screenshot or screen recording is the fastest path.

---

## Licensing

By contributing you agree your contributions are licensed under the
[Apache License 2.0](./LICENSE) to match the project. The patent grant
in that license protects both you and downstream users.
