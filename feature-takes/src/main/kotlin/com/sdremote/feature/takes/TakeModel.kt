package com.sdremote.feature.takes

/** Take metadata as exposed by the take list screen. */
data class TakeRow(
    val handle: Long,
    val scene: String,
    val take: Int,
    val durationStr: String,    // "00:43"
    val timeStr: String,        // "14:32"
    val circled: Boolean = false,
    val falseTake: Boolean = false,
    val note: String = "",
)

/** Sample data — mirrors JSX TAKE_LIST so Previews are 1:1 with mockups. */
val SampleTakes = listOf(
    TakeRow(1, "14A", 7, "00:43", "14:32", note = "Pickup on door"),
    TakeRow(2, "14A", 6, "01:02", "14:24", circled = true, note = "Buy — clean wide"),
    TakeRow(3, "14A", 5, "00:11", "14:21", falseTake = true, note = "Boom hit"),
    TakeRow(4, "14A", 4, "00:58", "14:14"),
    TakeRow(5, "14A", 3, "00:49", "14:08", note = "Plane overhead"),
    TakeRow(6, "14A", 2, "01:11", "14:02", circled = true, note = "Director loved it"),
    TakeRow(7, "14A", 1, "00:21", "13:56", falseTake = true, note = "False start"),
    TakeRow(8, "13C", 4, "02:05", "13:42", circled = true),
    TakeRow(9, "13C", 3, "00:33", "13:37"),
)

/** Filter chips on the take list. */
enum class TakeFilter(val label: String) {
    All("All"),
    Circled("Circled"),
    False("False"),
    NoNote("No Note"),
    Scene14A("14A"),
    Scene13C("13C"),
}
