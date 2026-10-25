package be.polus.tvlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

/** Leest de geïnstalleerde tv-apps uit de PackageManager. Alfabetisch gesorteerd. */
class AppRepository(private val context: Context) {

    fun load(): List<AppInfo> {
        val pm = context.packageManager
        return queryLeanbackActivities(pm)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { toAppInfo(pm, it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun queryLeanbackActivities(pm: PackageManager): List<ResolveInfo> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(query, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(query, 0)
        }
    }

    private fun toAppInfo(pm: PackageManager, info: ResolveInfo): AppInfo? = runCatching {
        val activity = info.activityInfo
        AppInfo(
            packageName = activity.packageName,
            label = info.loadLabel(pm).toString(),
            banner = activity.loadBanner(pm)?.toBitmap(BANNER_WIDTH, BANNER_HEIGHT)?.asImageBitmap(),
            icon = info.loadIcon(pm).toBitmap(ICON_SIZE, ICON_SIZE).asImageBitmap(),
        )
    }.getOrNull()

    private companion object {
        const val BANNER_WIDTH = 320
        const val BANNER_HEIGHT = 180
        const val ICON_SIZE = 192
    }
}
