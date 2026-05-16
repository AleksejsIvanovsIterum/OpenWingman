# OpenWingman

A modern, open-source Android remote control for Sound Devices field recorders
(MixPre, 833, Scorpio, and compatible BLE-enabled units).

> **Status**: Early development. UI scaffold and design system in place.
> Protocol implementation in progress. Not yet usable as a remote.

---

## Disclaimer

This is an **independent open-source project**, not affiliated with,
endorsed by, or supported by **Sound Devices, LLC**.

"Sound Devices", "MixPre", "MixPre II", "Scorpio", "888", "833", and
"Wingman" are trademarks of Sound Devices, LLC. Their use in this README
and inside the project is solely for the purpose of describing
hardware compatibility, as permitted under nominative fair use.

This project was developed through clean-room reverse engineering of the
publicly distributed Bluetooth Low Energy protocol used by Sound Devices
recorders, for the sole purpose of **interoperability**, as permitted under:
- **EU Directive 2009/24/EC**, Article 6 (decompilation for interoperability)
- **17 U.S.C. §1201(f)** (reverse engineering for interoperability)

No proprietary code, assets, or branding from Sound Devices' Wingman
application is included in this repository.

---

## Project goals

1. **Better field UX** — large touch targets, one-hand operation, dark-first.
2. **Modern stack** — Kotlin + Jetpack Compose + Material 3, native Android.
3. **Architecture** — protocol layer decoupled from transport (testable, replaceable).
4. **Open** — Apache 2.0 license, no telemetry, no analytics, no cloud.

---

## Status

| Component                | State        |
|--------------------------|--------------|
| Design tokens (light/dark) | ✅ Done       |
| Typography (JetBrains Mono + Inter) | ✅ Done |
| Primitives (icons, timecode, pill, transport buttons) | 🚧 In progress |
| Meters (Canvas-based) | 🚧 In progress |
| Frame (status bar, bottom nav) | 🚧 In progress |
| Transport screen (V6 default) | 🚧 In progress |
| Take list / Reports / Editor / Settings | ⏳ Planned |
| CLink protocol layer | ⏳ Planned |
| BLE transport | ⏳ Planned |
| Auto-reconnect, MTU negotiation | ⏳ Planned |
| Wear OS companion | 💭 Idea |

---

## Stack

- **Kotlin** 2.0 + **Jetpack Compose** + **Material 3**
- **Nordic Android BLE** (`no.nordicsemi.android:ble-ktx`)
- **Coroutines** + **Flow** for async
- **Koin** for DI
- **DataStore** for prefs
- Min SDK 26 (Android 8) — required for BLE5 features

---

## Project structure

```
:app                 — Compose entry, MainActivity, NavGraph
:core-ui             — Design system (theme, icons, primitives, meters, frame)
:core-protocol       — CLink protocol (pure Kotlin, no Android deps)
:core-transport      — Transport interface (planned)
:transport-ble       — Android BLE adapter (planned)
:transport-mock      — Fake transport for tests (planned)
:feature-transport   — Transport / metering screen (planned)
:feature-takes       — Take list, editor (planned)
:feature-reports     — Sound reports (planned)
:feature-settings    — Device settings (planned)
```

---

## Build (will work once UI scaffold is complete)

Requirements:
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34

```bash
git clone https://github.com/AleksejsIvanovsIterum/OpenWingman.git
cd OpenWingman
./gradlew assembleDebug
```

The resulting APK lands in `app/build/outputs/apk/debug/`.

---

## Contributing

PRs welcome. Two firm rules:

1. **No proprietary code.** Don't copy or paste decompiled bytecode or
   source from any Sound Devices software. If you've looked at the official
   APK, that's fine — but write contributions from the protocol spec, not
   from their code. Clean-room.
2. **No trademarks in code or UI.** Refer to recorders by generic terms
   where possible. Use "Sound Devices" / model names only in factual
   descriptions of compatibility.

See `CONTRIBUTING.md` (TBD) for details on the dev loop, code style,
and the protocol reference doc.

---

## License

[Apache License 2.0](./LICENSE)

The fonts shipped with this project are independently licensed:
- **JetBrains Mono** — [SIL Open Font License 1.1](https://www.jetbrains.com/lp/mono/)
- **Inter** — [SIL Open Font License 1.1](https://rsms.me/inter/)
