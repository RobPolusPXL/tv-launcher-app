package be.polus.tvlauncher

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val store = FavoritesStore(application)

    /** Alle geïnstalleerde apps, alfabetisch. */
    var allApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    /** De apps op Home, in de volgorde van de gebruiker. */
    var homeApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    var loaded by mutableStateOf(false)
        private set

    /** Wordt verhoogd als de Home-knop ingedrukt wordt terwijl de launcher al open is. */
    var homeSignal by mutableIntStateOf(0)
        private set

    private var order: List<String> = emptyList()
    private var moveSnapshot: List<String>? = null
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val apps = withContext(Dispatchers.Default) { repository.load() }
            val stored = store.read()
            val installed = apps.map { it.packageName }.toSet()

            // Eerste start: alles staat op Home. Later: nieuwe apps komen er niet automatisch bij.
            var next = stored ?: apps.map { it.packageName }
            if (apps.isNotEmpty()) next = next.filter { it in installed }

            allApps = apps
            if (next != stored) store.write(next)
            order = next
            rebuildHome()
            loaded = true
        }
    }

    fun goHome() {
        homeSignal++
    }

    fun addToHome(packageName: String) {
        if (packageName in order) return
        setOrder(order + packageName, persist = true)
    }

    fun removeFromHome(packageName: String) {
        setOrder(order - packageName, persist = true)
    }

    fun beginMove() {
        moveSnapshot = order
    }

    fun moveBy(packageName: String, delta: Int) {
        val from = order.indexOf(packageName)
        val to = from + delta
        if (from < 0 || to < 0 || to >= order.size) return
        val changed = order.toMutableList()
        changed.removeAt(from)
        changed.add(to, packageName)
        setOrder(changed, persist = false)
    }

    fun confirmMove() {
        moveSnapshot = null
        persist()
    }

    fun cancelMove() {
        moveSnapshot?.let { setOrder(it, persist = false) }
        moveSnapshot = null
    }

    private fun setOrder(newOrder: List<String>, persist: Boolean) {
        order = newOrder
        rebuildHome()
        if (persist) persist()
    }

    private fun rebuildHome() {
        val byPackage = allApps.associateBy { it.packageName }
        homeApps = order.mapNotNull { byPackage[it] }
    }

    private fun persist() {
        val snapshot = order
        viewModelScope.launch { store.write(snapshot) }
    }
}
