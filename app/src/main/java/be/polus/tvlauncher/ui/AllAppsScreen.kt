package be.polus.tvlauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import be.polus.tvlauncher.LauncherViewModel

/** Alle geïnstalleerde apps, alfabetisch. */
@Composable
fun AllAppsScreen(
    vm: LauncherViewModel,
    enabled: Boolean,
    onLaunch: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 96.u(), end = 96.u(), top = 72.u(), bottom = 56.u()),
        horizontalArrangement = Arrangement.spacedBy(24.u()),
        verticalArrangement = Arrangement.spacedBy(40.u()),
    ) {
        items(vm.allApps, key = { it.packageName }) { app ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                IconTile(app = app, enabled = enabled, onClick = { onLaunch(app.packageName) })
            }
        }
    }
}
