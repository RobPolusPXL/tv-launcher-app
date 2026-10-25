package be.polus.tvlauncher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import be.polus.tvlauncher.AppInfo

@Composable
fun LText(
    text: String,
    size: Int,
    color: Color,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.SemiBold,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(color = color, fontSize = size.us(), fontWeight = weight, textAlign = align),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Tab bovenaan. Focus = witte pil, geselecteerd = grijze pil. */
@Composable
fun TabPill(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val showFocus = focused && enabled
    val background = when {
        showFocus -> Color.White
        selected -> Palette.PillSelected
        else -> Color.Transparent
    }
    val textColor = when {
        showFocus -> Palette.OnLight
        selected -> Color.White
        else -> Palette.TextMuted
    }
    Box(
        modifier = Modifier
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .okKeys(onClick)
            .then(if (enabled) Modifier.focusable() else Modifier)
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 34.u(), vertical = 14.u()),
        contentAlignment = Alignment.Center,
    ) {
        LText(label, size = 30, color = textColor)
    }
}

/** Brede app-tegel voor Home. Toont de eigen banner van de app; zonder banner icoon + naam. */
@Composable
fun BannerTile(
    app: AppInfo,
    enabled: Boolean,
    moving: Boolean,
    editing: Boolean,
    requester: FocusRequester,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val showFocus = focused && enabled
    val scale by animateFloatAsState(if (showFocus) 1.1f else 1f, label = "tileScale")
    val shape = RoundedCornerShape(14.u())

    Box(
        modifier = Modifier
            .size(308.u(), 173.u())
            .zIndex(if (showFocus) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .focusRequester(requester)
            .onFocusChanged { focused = it.isFocused }
            .okKeys(onClick, onLongClick)
            .then(if (enabled) Modifier.focusable() else Modifier)
            .clip(shape)
            .background(Palette.Tile)
            .then(
                if (showFocus || moving) {
                    Modifier.border(5.u(), if (moving) Palette.Accent else Color.White, shape)
                } else {
                    Modifier
                },
            ),
    ) {
        val banner = app.banner
        if (banner != null) {
            Image(
                bitmap = banner,
                contentDescription = app.label,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.u()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.u(), Alignment.CenterVertically),
            ) {
                Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(72.u()))
                LText(app.label, size = 28, color = Palette.TextPrimary)
            }
        }
        if (editing && !moving) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.u())
                    .size(44.u())
                    .clip(CircleShape)
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center,
            ) {
                LText("⋯", size = 28, color = Color.White, align = TextAlign.Center)
            }
        }
    }
}

/** Gewone tegel aan het einde van de Home-rij ("Toevoegen", "Favorieten aanpassen"). */
@Composable
fun ActionTile(
    label: String,
    plus: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val showFocus = focused && enabled
    val scale by animateFloatAsState(if (showFocus) 1.1f else 1f, label = "actionScale")
    val shape = RoundedCornerShape(14.u())

    Box(
        modifier = Modifier
            .size(308.u(), 173.u())
            .zIndex(if (showFocus) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .okKeys(onClick)
            .then(if (enabled) Modifier.focusable() else Modifier)
            .clip(shape)
            .background(Palette.Tile)
            .then(if (showFocus) Modifier.border(5.u(), Color.White, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.u()),
        ) {
            if (plus) {
                Canvas(modifier = Modifier.size(48.u())) {
                    val s = size.minDimension
                    val stroke = s * 0.075f
                    drawLine(
                        color = Palette.TextPrimary,
                        start = Offset(s / 2f, s * 0.1f),
                        end = Offset(s / 2f, s * 0.9f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Palette.TextPrimary,
                        start = Offset(s * 0.1f, s / 2f),
                        end = Offset(s * 0.9f, s / 2f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            }
            LText(label, size = 28, color = Palette.TextPrimary, align = TextAlign.Center)
        }
    }
}

/** Vierkante app-tegel met icoon en naam, voor "Alle apps" en de toevoeg-lijst. */
@Composable
fun IconTile(
    app: AppInfo,
    enabled: Boolean = true,
    requester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val showFocus = focused && enabled
    val scale by animateFloatAsState(if (showFocus) 1.1f else 1f, label = "iconScale")
    val shape = RoundedCornerShape(36.u())

    Column(
        modifier = Modifier.width(176.u()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(26.u()),
    ) {
        Box(
            modifier = Modifier
                .size(176.u())
                .zIndex(if (showFocus) 1f else 0f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .then(if (requester != null) Modifier.focusRequester(requester) else Modifier)
                .onFocusChanged { focused = it.isFocused }
                .okKeys(onClick)
                .then(if (enabled) Modifier.focusable() else Modifier)
                .clip(shape)
                .background(Palette.Tile)
                .then(if (showFocus) Modifier.border(5.u(), Color.White, shape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(112.u()))
        }
        LText(
            text = app.label,
            size = 26,
            color = if (showFocus) Color.White else Palette.TextMuted,
            align = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun MenuItem(
    label: String,
    requester: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (requester != null) Modifier.focusRequester(requester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .okKeys(onClick)
            .focusable()
            .clip(RoundedCornerShape(14.u()))
            .background(if (focused) Color.White else Palette.Tile)
            .padding(horizontal = 28.u(), vertical = 22.u()),
    ) {
        LText(label, size = 32, color = if (focused) Palette.OnLight else Palette.TextPrimary)
    }
}
