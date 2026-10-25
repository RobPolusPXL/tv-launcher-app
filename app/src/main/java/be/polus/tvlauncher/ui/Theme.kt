package be.polus.tvlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Kleuren van de launcher: vlak en donker, in de stijl van de originele Android TV-home. */
object Palette {
    val BackgroundTop = Color(0xFF23262B)
    val BackgroundBottom = Color(0xFF15171A)
    val PillSelected = Color(0xFF3A3E44)
    val TextPrimary = Color(0xFFE8EAED)
    val TextMuted = Color(0xFFA8ADB3)
    val Tile = Color(0xFF2E3237)
    val Panel = Color(0xFF23262B)
    val Accent = Color(0xFFF2C46D)
    val Scrim = Color(0xCC000000)
    val OnLight = Color(0xFF15171A)
}

/**
 * Eén "pixel" uit het ontwerp (gemaakt op een scherm van 1920 breed), omgerekend naar dp.
 * Zo ziet de launcher er op elke tv-resolutie en -dichtheid hetzelfde uit.
 */
val LocalUnit = compositionLocalOf { 1.dp }

@Composable
fun Int.u(): Dp = (this * LocalUnit.current.value).dp

@Composable
fun Int.us(): TextUnit = (this * LocalUnit.current.value).sp
