package be.polus.tvlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import be.polus.tvlauncher.LauncherViewModel

@Composable
fun LauncherApp(
    vm: LauncherViewModel,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val ui = remember { LauncherUi() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Palette.BackgroundTop, Palette.BackgroundBottom))),
    ) {
        val unit = maxWidth / 1920f
        CompositionLocalProvider(LocalUnit provides unit) {
            LauncherContent(vm, ui, onLaunch, onOpenSettings)
        }
    }
}

@Composable
private fun LauncherContent(
    vm: LauncherViewModel,
    ui: LauncherUi,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Home-knop terwijl de launcher al open is: terug naar het beginscherm.
    LaunchedEffect(vm.homeSignal) {
        ui.tab = Tab.Home
        ui.editMode = false
        ui.menuPackage = null
        ui.pickerOpen = false
        if (ui.movingPackage != null) {
            vm.cancelMove()
            ui.movingPackage = null
        }
    }

    // De Terug-knop sluit eerst wat open staat. Op Home zelf doet hij niets.
    BackHandler {
        val menu = ui.menuPackage
        val moving = ui.movingPackage
        when {
            ui.pickerOpen -> ui.pickerOpen = false

            menu != null -> {
                ui.focusRestore = menu
                ui.menuPackage = null
            }

            moving != null -> {
                vm.cancelMove()
                ui.focusRestore = moving
                ui.movingPackage = null
            }

            ui.editMode -> ui.editMode = false
            ui.tab != Tab.Home -> ui.tab = Tab.Home
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 56.u())) {
        TopTabs(
            ui = ui,
            enabled = !ui.overlayOpen && ui.movingPackage == null,
            onOpenSettings = onOpenSettings,
        )
        when (ui.tab) {
            Tab.Home -> HomeScreen(vm = vm, ui = ui, onLaunch = onLaunch)
            Tab.AllApps -> AllAppsScreen(vm = vm, enabled = !ui.overlayOpen, onLaunch = onLaunch)
        }
    }

    ui.menuPackage?.let { packageName ->
        val title = vm.allApps.firstOrNull { it.packageName == packageName }?.label ?: packageName
        ContextMenu(
            title = title,
            onMove = {
                ui.menuPackage = null
                ui.movingPackage = packageName
                vm.beginMove()
                ui.focusRestore = packageName
            },
            onRemove = {
                val index = vm.homeApps.indexOfFirst { it.packageName == packageName }
                ui.menuPackage = null
                vm.removeFromHome(packageName)
                val remaining = vm.homeApps
                ui.focusRestore = remaining.getOrNull(index.coerceAtMost(remaining.lastIndex))?.packageName
            },
        )
    }

    if (ui.pickerOpen) {
        AppPicker(vm = vm)
    }
}

@Composable
private fun TopTabs(
    ui: LauncherUi,
    enabled: Boolean,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 96.u()),
        horizontalArrangement = Arrangement.spacedBy(12.u()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabPill(
            label = "Home",
            selected = ui.tab == Tab.Home,
            enabled = enabled,
            onFocused = { ui.tab = Tab.Home },
            onClick = {},
        )
        TabPill(
            label = "Alle apps",
            selected = ui.tab == Tab.AllApps,
            enabled = enabled,
            onFocused = { ui.tab = Tab.AllApps },
            onClick = {},
        )
        // Opent gewoon de systeeminstellingen van de tv.
        TabPill(
            label = "Instellingen",
            selected = false,
            enabled = enabled,
            onFocused = {},
            onClick = onOpenSettings,
        )
    }
}
