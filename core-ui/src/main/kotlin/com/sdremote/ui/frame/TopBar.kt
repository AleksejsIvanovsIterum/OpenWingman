package com.sdremote.ui.frame

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdremote.ui.components.WmLabel
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Screen header — JSX `WMTopBar`.
 *
 * Title in tcLarge / sceneTake style, optional subtitle (caption above),
 * right slot for actions (search, plus, status pill, etc).
 */
@Composable
fun WmTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    rightSlot: @Composable () -> Unit = {},
) {
    val tokens = Wm.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, tokens.border, RectangleShape)
            .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            if (subtitle != null) WmLabel(text = subtitle)
            Text(
                text = title,
                color = tokens.ink,
                style = Wm.type.tcLarge,
                modifier = Modifier.padding(top = if (subtitle != null) 2.dp else 0.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) { rightSlot() }
    }
}

@Preview(name = "TopBar dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 393)
@Composable
private fun TopBarDark() {
    WmTheme(darkTheme = true) {
        WmTopBar(title = "Take List", subtitle = "A_Wedding_Day · 142 takes")
    }
}
