# Luontopeli

Luontopeli on Android-sovellus, joka kannustaa käyttäjiä liikkumaan luonnossa, seuraamaan kävelytilastojaan ja tunnistamaan kohtaamiaan kasveja ja muita luontokohteita tekoälyn avulla.

## Pääominaisuudet

*   **Interaktiivinen Kartta:** Reaaliaikainen sijainnin seuranta OpenStreetMapin (osmdroid) avulla. Sovellus piirtää kuljetun reitin ja näyttää käyttäjän sijainnin.
*   **Kävelyn seuranta:** Mittaa askeleet, kuljetun matkan ja ajan. Sisältää live-ajastimen ja integroinnin laitteen omiin sensoreihin.
*   **Älykäs Luontokamera:** Ota valokuvia luontokohteista. Sovellus käyttää Google ML Kit -tekoälyä tunnistaakseen kasvit ja muut luontokohteet.
*   **Löytögalleria:** Selaa kaikkia tallentamiasi luontolöytöjä kuvineen ja aikaleimoineen.
*   **Tilastot:** Kattava yhteenveto kaikista kävelylenkeistäsi ja löydöistäsi.
*   **Offline-first:** Sovellus toimii täysin ilman internetyhteyttä ja tallentaa kaiken datan paikalliseen tietokantaan.

## Teknologiat ja Kirjastot

*   **Kieli:** Kotlin
*   **Käyttöliittymä:** Jetpack Compose (Material 3)
*   **Arkkitehtuuri:** MVVM (Model-View-ViewModel)
*   **Riippuvuusinjektio:** Hilt (Dagger)
*   **Tietokanta:** Room Persistence Library (SQLite)
*   **Kamera:** CameraX
*   **Tekoäly:** Google ML Kit Image Labeling (On-device)
*   **Kartat:** osmdroid (OpenStreetMap)
*   **Kuvien lataus:** Coil
*   **Luvanhallinta:** Accompanist Permissions

## Projektin rakenne

*   `camera/`: Kameranäkymä ja kuvanotto-logiikka.
*   `data/`: Tietokantamääritykset (Room), DAO:t ja Repository-luokat.
*   `location/`: GPS-seuranta ja sijaintipalvelut.
*   `ml/`: Tekoälymallit ja tunnistustulosten käsittely.
*   `sensor/`: Askelmittarin ja gyroskoopin hallinta.
*   `ui/`: Compose-näkymät (Kartta, Tilastot, Löydöt) ja teeman määritykset.
*   `viewmodel/`: Sovelluksen logiikka ja tilanhallinta.
