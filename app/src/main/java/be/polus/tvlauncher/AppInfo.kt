package be.polus.tvlauncher

import androidx.compose.ui.graphics.ImageBitmap

/** Een app die op de tv gestart kan worden. [banner] is de eigen banner van de app, indien aanwezig. */
data class AppInfo(
    val packageName: String,
    val label: String,
    val banner: ImageBitmap?,
    val icon: ImageBitmap,
)
