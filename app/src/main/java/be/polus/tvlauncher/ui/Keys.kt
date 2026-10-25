package be.polus.tvlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * OK-knop van de afstandsbediening: kort indrukken = [onClick], lang indrukken = [onLongClick].
 *
 * Een loslaat-event telt alleen mee als dezelfde tegel ook de indruk-kant zag. Anders zou het
 * loslaten na een lange druk meteen het eerste menu-item activeren dat net opende.
 */
@Composable
fun Modifier.okKeys(onClick: () -> Unit, onLongClick: (() -> Unit)? = null): Modifier {
    var downSeen by remember { mutableStateOf(false) }
    var longDone by remember { mutableStateOf(false) }
    return onKeyEvent { event ->
        if (!event.isSelectKey()) return@onKeyEvent false
        when (event.type) {
            KeyEventType.KeyDown -> {
                if (event.nativeKeyEvent.repeatCount == 0) downSeen = true
                if (onLongClick != null && downSeen && !longDone && event.nativeKeyEvent.isLongPress) {
                    longDone = true
                    onLongClick()
                }
                true
            }

            KeyEventType.KeyUp -> {
                val handled = downSeen
                if (downSeen && !longDone) onClick()
                downSeen = false
                longDone = false
                handled
            }

            else -> false
        }
    }
}

private fun KeyEvent.isSelectKey(): Boolean =
    key == Key.DirectionCenter || key == Key.Enter || key == Key.NumPadEnter
