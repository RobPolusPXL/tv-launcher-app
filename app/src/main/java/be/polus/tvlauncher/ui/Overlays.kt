package be.polus.tvlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import be.polus.tvlauncher.LauncherViewModel

/** Menu bij een tegel op Home: Verplaatsen of Verwijderen van Home (de app blijft geïnstalleerd). */
@Composable
fun ContextMenu(
    title: String,
    onMove: () -> Unit,
    onRemove: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        runCatching { first.requestFocus() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(620.u())
                .clip(RoundedCornerShape(24.u()))
                .background(Palette.Panel)
                .padding(36.u()),
            verticalArrangement = Arrangement.spacedBy(12.u()),
        ) {
            LText(
                text = title,
                size = 36,
                color = Palette.TextPrimary,
                modifier = Modifier.padding(bottom = 12.u()),
            )
            MenuItem(label = "Verplaatsen", requester = first, onClick = onMove)
            MenuItem(label = "Verwijderen van Home", requester = null, onClick = onRemove)
        }
    }
}

/** Lijst met de apps die nog niet op Home staan. OK voegt een app achteraan Home toe. */
@Composable
fun AppPicker(vm: LauncherViewModel) {
    val onHome = vm.homeApps.map { it.packageName }.toSet()
    val candidates = vm.allApps.filter { it.packageName !in onHome }
    val first = remember { FocusRequester() }

    LaunchedEffect(candidates.size) {
        if (candidates.isNotEmpty()) {
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(1500.u())
                .clip(RoundedCornerShape(24.u()))
                .background(Palette.Panel)
                .padding(48.u()),
            verticalArrangement = Arrangement.spacedBy(32.u()),
        ) {
            LText(text = "Apps toevoegen aan Home", size = 40, color = Palette.TextPrimary)
            if (candidates.isEmpty()) {
                LText(text = "Alle apps staan al op Home.", size = 30, color = Palette.TextMuted)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.u()),
                    contentPadding = PaddingValues(vertical = 24.u()),
                    horizontalArrangement = Arrangement.spacedBy(24.u()),
                    verticalArrangement = Arrangement.spacedBy(40.u()),
                ) {
                    itemsIndexed(candidates, key = { _, app -> app.packageName }) { index, app ->
                        IconTile(
                            app = app,
                            requester = if (index == 0) first else null,
                            onClick = { vm.addToHome(app.packageName) },
                        )
                    }
                }
            }
            LText(text = "OK: toevoegen     Terug: sluiten", size = 26, color = Palette.TextMuted)
        }
    }
}
