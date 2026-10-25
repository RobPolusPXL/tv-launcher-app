package be.polus.tvlauncher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Tab { Home, AllApps }

/** Tijdelijke schermstatus van de launcher. De apps en hun volgorde zitten in de ViewModel. */
class LauncherUi {
    var tab by mutableStateOf(Tab.Home)

    /** "Favorieten aanpassen" aan: OK op een tegel opent het menu in plaats van de app te starten. */
    var editMode by mutableStateOf(false)

    /** Package waarvoor het menu (Verplaatsen / Verwijderen van Home) open staat. */
    var menuPackage by mutableStateOf<String?>(null)

    /** Package die nu verplaatst wordt. */
    var movingPackage by mutableStateOf<String?>(null)

    var pickerOpen by mutableStateOf(false)

    /** Tegel die na het sluiten van een menu opnieuw de focus moet krijgen. */
    var focusRestore by mutableStateOf<String?>(null)

    var initialFocusDone by mutableStateOf(false)

    val overlayOpen: Boolean
        get() = menuPackage != null || pickerOpen
}
