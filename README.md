# TV Launcher

Minimalistische Android TV-launcher. Geen aanbevelingen, geen reclame, geen netwerk.

- **Home:** eigen gekozen apps in zelf bepaalde volgorde, met de eigen banner van elke app
- **Alle apps:** alles wat geïnstalleerd is, alfabetisch
- **Instellingen:** opent de systeeminstellingen van de tv
- OK lang indrukken op een tegel: *Verplaatsen* of *Verwijderen van Home* (de app blijft geïnstalleerd)
- Aan het einde van de rij: *Toevoegen* en *Favorieten aanpassen*

## Bouwen

```
./gradlew assembleDebug
```

De APK staat daarna in `app/build/outputs/apk/debug/app-debug.apk`. Op GitHub bouwt de workflow *Build APK* hem bij elke push.

## Installeren

```
adb connect <tv-ip>:5555
adb install -r app-debug.apk
```

De launcher verschijnt eerst als gewone app en kan zo getest worden. Pas daarna de standaardlauncher uitzetten:

```
adb shell pm list packages | grep launcher
adb shell pm disable-user --user 0 <package>
```

Terugzetten: `adb shell pm enable <package>`.
