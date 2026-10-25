package be.polus.tvlauncher

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.launcherDataStore by preferencesDataStore(name = "launcher")

/** Bewaart de apps op Home, in de volgorde die de gebruiker koos (package names). */
class FavoritesStore(private val context: Context) {

    private val orderKey = stringPreferencesKey("home_order")

    /** `null` = nog nooit opgeslagen (eerste start). Een lege lijst betekent: bewust leeg. */
    suspend fun read(): List<String>? {
        val raw = context.launcherDataStore.data.first()[orderKey] ?: return null
        return if (raw.isEmpty()) emptyList() else raw.split(SEPARATOR)
    }

    suspend fun write(order: List<String>) {
        context.launcherDataStore.edit { it[orderKey] = order.joinToString(SEPARATOR) }
    }

    private companion object {
        const val SEPARATOR = ","
    }
}
