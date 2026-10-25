package be.polus.tvlauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import be.polus.tvlauncher.AppInfo
import be.polus.tvlauncher.LauncherViewModel

@Composable
fun HomeScreen(
    vm: LauncherViewModel,
    ui: LauncherUi,
    onLaunch: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val requesters = remember { mutableMapOf<String, FocusRequester>() }
    val apps = vm.homeApps
    val tilesEnabled = !ui.overlayOpen
    val moving = ui.movingPackage != null

    // Eénmalig bij het opstarten: focus op de eerste tegel.
    LaunchedEffect(vm.loaded) {
        if (!vm.loaded || ui.initialFocusDone) return@LaunchedEffect
        val first = vm.homeApps.firstOrNull() ?: return@LaunchedEffect
        withFrameNanos { }
        runCatching { requesters[first.packageName]?.requestFocus() }
        ui.initialFocusDone = true
    }

    // Na een menu of verplaatsing: focus terug op de bedoelde tegel.
    LaunchedEffect(ui.focusRestore) {
        val packageName = ui.focusRestore ?: return@LaunchedEffect
        val index = vm.homeApps.indexOfFirst { it.packageName == packageName }
        if (index >= 0) {
            runCatching { listState.scrollToItem(index) }
            withFrameNanos { }
            runCatching { requesters[packageName]?.requestFocus() }
        }
        ui.focusRestore = null
    }

    fun openMenu(app: AppInfo) {
        ui.menuPackage = app.packageName
    }

    fun onTileClick(app: AppInfo) {
        val movingPackage = ui.movingPackage
        when {
            movingPackage == app.packageName -> {
                vm.confirmMove()
                ui.movingPackage = null
            }

            movingPackage != null -> Unit
            ui.editMode -> openMenu(app)
            else -> onLaunch(app.packageName)
        }
    }

    fun onTileLongClick(app: AppInfo) {
        if (ui.movingPackage == null) openMenu(app)
    }

    // Tijdens het verplaatsen sturen links/rechts de gekozen app; omhoog/omlaag doen niets.
    fun handleMoveKeys(event: KeyEvent): Boolean {
        val movingPackage = ui.movingPackage ?: return false
        if (event.type != KeyEventType.KeyDown) return false
        return when (event.key) {
            Key.DirectionLeft -> {
                vm.moveBy(movingPackage, -1)
                true
            }

            Key.DirectionRight -> {
                vm.moveBy(movingPackage, 1)
                true
            }

            Key.DirectionUp, Key.DirectionDown -> true
            else -> false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.u()),
        verticalArrangement = Arrangement.Bottom,
    ) {
        LText(
            text = "Favorieten",
            size = 32,
            color = Palette.TextPrimary,
            modifier = Modifier.padding(start = 108.u()),
        )
        Spacer(Modifier.height(4.u()))
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { handleMoveKeys(it) },
            contentPadding = PaddingValues(start = 108.u(), end = 96.u(), top = 28.u(), bottom = 28.u()),
            horizontalArrangement = Arrangement.spacedBy(28.u()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(apps, key = { it.packageName }) { app ->
                val requester = requesters.getOrPut(app.packageName) { FocusRequester() }
                BannerTile(
                    app = app,
                    enabled = tilesEnabled,
                    moving = ui.movingPackage == app.packageName,
                    editing = ui.editMode,
                    requester = requester,
                    onClick = { onTileClick(app) },
                    onLongClick = { onTileLongClick(app) },
                )
            }
            item(key = "add") {
                ActionTile(
                    label = "Toevoegen",
                    plus = true,
                    enabled = tilesEnabled && !moving,
                    onClick = { ui.pickerOpen = true },
                )
            }
            item(key = "edit") {
                ActionTile(
                    label = if (ui.editMode) "Klaar" else "Favorieten aanpassen",
                    plus = false,
                    enabled = tilesEnabled && !moving,
                    onClick = { ui.editMode = !ui.editMode },
                )
            }
        }
        if (moving) {
            LText(
                text = "Links/rechts: verplaatsen     OK: bevestigen     Terug: annuleren",
                size = 26,
                color = Palette.TextMuted,
                modifier = Modifier.padding(start = 108.u(), top = 8.u()),
            )
        }
    }
}
