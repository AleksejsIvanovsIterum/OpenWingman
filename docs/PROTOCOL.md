# CLink Protocol — clean-room specification

This document describes the **CLink** protocol used between Sound Devices
field recorders (MixPre series, 833, 888, Scorpio) and remote-control
clients over Bluetooth Low Energy.

The specification was derived through clean-room reverse engineering of a
publicly distributed APK for the purpose of **interoperability**, as
permitted under EU Directive 2009/24/EC Article 6 and 17 U.S.C. §1201(f).

> **Status.** Independent description; not endorsed by Sound Devices, LLC.
> Field values that look like trademarks (model names, "Wingman") are
> factual labels for compatibility purposes and are reproduced under
> nominative fair use. The information here describes facts about wire
> behaviour, which are uncopyrightable.

---

## 1. Transport — BLE GATT

CLink is a request/response protocol over two GATT characteristics on a
proprietary Sound Devices service.

### 1.1 Service / characteristics

| Role | UUID |
|---|---|
| Sound Devices service | `91a7b1c3-f9d5-4679-9d26-e134067591fb` |
| TX — client → device (write w/o response) | `e66f3aa8-e06b-4b7d-a77e-3aa08ebb3f7f` |
| RX — device → client (notify, requires CCCD enable) | `5049f187-3415-4e2d-bad5-00be79793d27` |

Standard SIG services are also used:

| Role | UUID |
|---|---|
| Device Information Service | `0x180A` |
| Firmware Revision String | `0x2A26` |

### 1.2 Discovery and advertisement payload

Devices advertise the Sound Devices service UUID and include data in the
SIG Device Information service entry. The 0x180A service-data record
contains:

| Offset | Type | Field |
|---|---|---|
| 0 | u8 | Product ID (see §5.1) |
| 1 | u8 | Firmware major |
| 2 | u8 | Firmware minor |
| 3..14 | ASCII | Serial number, NUL-terminated (max 12 chars) |

### 1.3 MTU and chunking

The original Wingman client uses the default ATT MTU of 23 bytes and
chunks every write into 20-byte slices with a 20 ms delay between them.
This is conservative; modern Android stacks routinely negotiate MTU up to
247. OpenWingman negotiates `MTU = 247` post-connect and chunks at
`MTU - 3` byte boundaries, which improves throughput by ~12× on large
transfers (e.g. SoundReport download).

---

## 2. Frame structure

All multi-byte values are **little-endian**.

```
Offset  Width  Field
0       1      Header constant = 0xA5
1       1      Unit ID — 0x00 from client; 0xF0 ACK; 0xF1 DATA; 0xFE broadcast
2       1      Data length = command + payload size (not counting checksum)
3       1      Command (see §3)
4..N    var    Payload (command-specific)
N+1     1      Checksum byte 1
N+2     1      Checksum byte 2

Total = 3 + dataLen + 2
```

### 2.1 Special Unit-ID values

| Value | Meaning |
|---|---|
| 0x00 | Acknowledge success (in ACK frames) |
| 0xF0 | Acknowledge response |
| 0xF1 | Data response |
| 0xFE | Broadcast to all units |

### 2.2 Checksum

Fletcher-style accumulator with mod 255 (not 256) and a final inversion
pass. Crucially, the Unit-ID byte is **substituted with 0** for non-broadcast
frames — presumably so the checksum is unit-agnostic in routing scenarios.

```c
uint8_t ck1 = 0, ck2 = 0;
for (i = 0; i < n; i++) {
    uint8_t b = frame[i];
    if (i == 1 && frame[1] != 0xFE) b = 0;  // unit substitution
    ck1 = (ck1 + b)   % 255;
    ck2 = (ck2 + ck1) % 255;
}
int s = (ck1 + ck2) % 255;
ck1_final = 255 - s;
ck1_acc   = (ck1 + ck1_final) % 255;
ck2_final = 255 - ck1_acc;
```

Verification: re-run the accumulator over header + unit + length +
command + payload + ck1 + ck2; both accumulators must end at 0.

---

## 3. Commands

Codes are decimal. *Direction* notes which side originates the frame.

| Code | Name | Dir | Purpose |
|---:|---|---|---|
| 1 | GetVersion | req | Firmware version (u16) |
| 12 | SetTimeDate | req | RTC update |
| 14 | GetTimeDate | req | Read RTC |
| 17 | TransportControl | req | REC / PLAY / STOP / FF / REW |
| 18 | TransportStatus | req | Current transport state |
| 19 | GetTimecode | req | Generator + Received + FileTime |
| 56 | GetTakeList | req | List take handles in a range |
| 57 | GetTakeParameter | req | Per-take metadata |
| 58 | SetTakeParameter | req | Modify per-take metadata |
| 59 | FalseTake | req | Mark a take as false |
| 60 | GetSetting | req | Read one of the system settings |
| 61 | SetSetting | req | Modify a system setting |
| 62 | GetMeters | req | (Legacy) meter levels |
| 65 | GetDeviceInfo | req | ProductID / serial / FW |
| 78 | GetTakeListChangeStatus | req | Take-list change polling |
| 81 | SetInputRouting | req | I/O routing matrix |
| 82 | GetInputRouting | req | Read routing matrix |
| 85 | GetParameterChangeStatus | req | Parameter change polling |
| 88 | GetDriveStatus | req | Media status + size (free/used/total) |
| 89 | GetPowerVoltage / Status | req | Battery / power |
| 90 | SetTimecodeGenerator | req | TC framerate, mode, jam |
| 91 | SetBaudRate | req | Serial link baud (LTC) |
| 92 | GetRecordFolder | req | Current record folder name |
| 93 | SetRecordFolder | req | Change record folder |
| 94 | GetChannelLinking | req | Input/output linking bitmap |
| 95 | SetChannelLinking | req | Modify linking |
| **96** | **Authenticate** | both | Challenge-response handshake (see §4) |
| 97 | GetExtendedParameterChangeStatus | req | Meters + change flags (cf. §6.1) |
| 98 | GetDynSettingsList | req | Scene / preset lists |
| 99 | SetDynSettingsList | req | Mutate list |
| 100 | SoundReport | req | Sound-report file transfer |
| 101 | MediaList | req | Files on media |
| 102 | ShowMessage | req | Display text on recorder |
| 103 | ValidatePassword | req | Remote password gate |
| 104 | GetUnitMode | req | Basic/Advanced/Custom/FileTransfer/Music |

### 3.1 Response codes (ACK frames)

The first payload byte of an ACK identifies success / failure:

| Code | Meaning |
|---:|---|
| 0 | Success |
| 1 | CommandNotSupported |
| 2 | InvalidParameters |
| 3 | Busy (caller should retry) |
| 4 | ParameterOutOfRange |
| 5 | NoResponse |
| 6 | CommandSpecific (sub-codes in subsequent bytes) |
| 7 | OperationFailure (often: password gate required) |

---

## 4. Authentication

### 4.1 Dongle handshake

Required on every connect.

1. Client → device: `[A5][00][01][60][...ck]` (empty Authenticate request).
2. Device → client: data frame with payload `[0x04][len][challenge_bytes...]`.
3. Client computes `response = SHA-1(challenge || AUTH_KEY)`, 20 bytes.
4. Client → device: `[A5][00][16][60][0x01][0x14][hash20][ck1][ck2]`.
5. Device → client: ACK with success (or closes connection on failure).

**AUTH_KEY** is an 8-byte hard-coded constant present in every Wingman
release we have observed: `38 6A 9B 16 07 68 3A 34`.

This is not a cryptographic secret in the conventional sense — anyone
with the public APK can recover it. It is, by Sound Devices' design, a
"trusted-client" gate.

### 4.2 Remote password

If the recorder has a remote password configured, the *first command*
after dongle auth returns `OperationFailure (7)`. The client must then:

1. Hash the user-entered password: `pw_hash = SHA-1(utf8(password))`.
2. Send `ValidatePassword (103)`: `[0x14][pw_hash[20]]`.
3. On success, retry the original command.

The hash is not salted and is plain SHA-1; the same value is the password
file on the device, so changing the algorithm requires firmware changes
on Sound Devices' side.

---

## 5. Selected payload schemas

### 5.1 ProductID enum (advertisement + cmd 65)

| Value | Display |
|---:|---|
| 0 | MixPre-3 |
| 1 | MixPre-6 |
| 2 | MixPre-10T |
| 3 | MixPre-3 II |
| 4 | MixPre-6 II |
| 5 | MixPre-10 II |
| 6 | 833 |
| 7 | 888 |
| 8 | Scorpio |

(More may exist for newer firmware; treat unknown values as `Unknown`.)

### 5.2 TransportControl (cmd 17) / TransportStatus (cmd 18)

Payload is a single byte from the TransportState enum:

| Value | State |
|---:|---|
| 0 | Stop |
| 1 | Play |
| 2 | FastForward |
| 3 | Rewind |
| 4 | Record |
| 5 | Pause |
| 6 | Idle |

When the recorder rejects TransportControl with `CommandSpecific`, the
second payload byte holds a MixPreTransportError code:
`NoMedia (1) | NoTracks (2) | InvalidSrate (3) | Busy (4) | InvalidFile (5) |
MediaFull (6) | TooManyTracks (7)`.

### 5.3 GetTimecode (cmd 19) response

12-byte payload, three timecodes back-to-back. Within each, the **byte
order is FF, SS, MM, HH** (note: not the usual reading order):

```
Offset  Field
0..3    Generator TC
4..7    Received  TC
8..11   FileTime  TC
```

### 5.4 GetDeviceInfo (cmd 65) — parameter-driven

Request payload: `[DeviceInfoParam]` (one byte).

| Param | Code | Response shape |
|---|---:|---|
| ProductID | 0 | `[productId u8]` |
| SerialNumber | 1 | NUL-terminated UTF-8 |
| SoftwareVersion | 2 | `[minor u8][major u8]` (0xFF→1 on major) |
| Cl12SoftwareVersion | 3 | `[minor u8][major u8]` |

### 5.5 GetSetting (cmd 60) / SetSetting (cmd 61)

Payload: `[settingId u16 LE][value u16 LE]` (request also omits value
for GetSetting).

Selected setting IDs:

| ID | Name | Value shape |
|---:|---|---|
| 0 | SampleRate | enum (see source) |
| 1 | BitDepth | u16 |
| 2 | RecordTrackArms | bitmap u16 |
| 3 | InputEnableBitmap | bitmap u16 |
| 4 | TimecodeFrameRate | enum |
| 5 | MeterBallistics | 0=VU, 2=Peak, else=PeakVU |
| 6 | PeakHold | u16 (ms) |
| 13 | PlaybackEnableBitmap | bitmap u16 |
| 14..19 | ChannelN Linking (N=1,3,5,7,9,11) | bitmap u16 |

### 5.6 GetDriveStatus (cmd 88) response

| Offset | Type | Field |
|---:|---|---|
| 0 | u8 | Status flags |
| 1..4 | u32 LE | Free bytes |
| 5..8 | u32 LE | Used bytes |
| 9..12 | u32 LE | Total bytes |

> **Note.** The original Wingman client reads the three u32 values but
> never plumbs them through to the UI — the user only sees the status
> byte. OpenWingman surfaces them.

### 5.7 GetExtendedParameterChangeStatus (cmd 97) response

The "meter pump" — sent every ~33 ms by the official client. The
response is a single packet with all per-channel data and parameter
change flags.

```
Group:
  [+0]  numChannels (u8)
  [+1+] numChannels × { VU u8, Peak u8, Extra u8 }

Groups in order:
  1) Inputs
  2) Outputs
  3) Returns
  4) Tracks
  5) Aux  (MixPre family only)

Trailing parameter change struct (10 bytes):
  [+0]   takeFlags u8
  [+1]   takeHandle u32 LE
  [+5]   takeInfo1 u8
  [+6]   takeInfo2 u8
  [+7..9] pFlags u8 × 3 — flag bitmaps for what changed
```

### 5.8 GetUnitMode (cmd 104) response

Reports the active recorder mode plus enabled feature bitmaps:

| Offset | Type | Field |
|---:|---|---|
| 0..1 | u16 LE | DeviceMode (Basic/Advanced/Custom/FileTransfer/Music) |
| 2..5 | u32 LE | FeatureGroups bitmap |
| 6..7 | u16 LE | RecordOptions bitmap |
| 8..9 | u16 LE | ChannelOptions bitmap |
| 10..11 | u16 LE | reserved |
| 12..13 | u16 LE | MetadataOptions bitmap |

### 5.9 SoundReport (cmd 100) flow

Streaming download of the device-generated CSV report:

```
client → device: [0x00 StartCreation, drive u8, path... 0]    → ACK
client → device: [0x01 GetStatus]                              → DATA [statusByte]
client → device: [0x02 OpenReport]                             → DATA [fileLength u64 LE]
client → device: [0x03 ReadData]                               → DATA [bytes...] (repeat)
client → device: [0x04 CloseReport]                            → ACK
```

---

## 6. Best practices

### 6.1 Meter polling

The original Wingman polls `cmd 97` at ~30 Hz. OpenWingman uses an
adaptive cadence (10 Hz idle, 30 Hz when transport is recording) to
preserve battery. The response is large enough to fit one packet at
MTU 247 — never split across BLE notifications in our testing.

### 6.2 Resend strategy

The protocol does not include sequence numbers or selective retransmission.
The C# implementation retains the last-sent buffer as a `ResendableBytes`
slot and re-transmits if no response arrives within 4 s. OpenWingman uses
a 4 s per-command timeout and reports `ResponseTimeout` upstream rather
than transparently retrying — application-layer retries are easier to
reason about.

### 6.3 Busy handling

`Busy (3)` is common during heavy I/O (sound report download, large
take-list pagination). Wait ~200 ms and resend the same command verbatim.
Most clients implement exponential backoff capped at 4 retries.

### 6.4 Don't trust the checksum

The original Wingman implementation calls its own `IsChecksumsInPacketValid()`
but **discards the return value** and accepts the frame regardless. This
means corrupt frames are routinely accepted by the official client.
OpenWingman validates the checksum on every received frame.

---

## 7. Reference values

| Constant | Value |
|---|---|
| `HEADER` | `0xA5` |
| `AUTH_KEY` | `38 6A 9B 16 07 68 3A 34` |
| `RESPONSE_SIZE` | 20 bytes (SHA-1 digest) |
| `ResponseTimeout` | 4 s |
| Default ATT MTU (Wingman) | 23 |
| OpenWingman negotiated MTU | 247 |
| Per-write chunk (Wingman) | 20 bytes |
| Per-write chunk (OpenWingman) | MTU − 3 |

---

## 8. References

- Sound Devices Wingman product page (model compatibility list):
  <https://www.sounddevices.com/product/wingman/>
- IEEE Std for SIG Bluetooth GATT base UUID (`...0000-1000-8000-00805F9B34FB`).
- 17 U.S.C. §1201(f) — Reverse engineering for interoperability.
- Directive 2009/24/EC, Article 6 — Decompilation for interoperability.
