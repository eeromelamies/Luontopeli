package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository-luokka luontolöytöjen hallintaan.
 *
 * Toimii välittäjänä tietolähteiden (Room-tietokanta) ja ViewModelien välillä.
 * @Singleton varmistaa että meillä on vain yksi repository-instanssi koko sovelluksessa.
 */
@Singleton
class NatureSpotRepository @Inject constructor(
    private val dao: NatureSpotDao
) {
    /** Flow-virta kaikista luontolöydöistä aikajärjestyksessä (uusin ensin) */
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    /** Flow-virta löydöistä joilla on validi GPS-sijainti (kartalla näytettävät) */
    val spotsWithLocation: Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    /**
     * Tallentaa uuden luontolöydön paikalliseen tietokantaan.
     * Offline-vaiheessa käytetään oletuskäyttäjää.
     */
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(
            userId = "offline_user", // Päivitetään Firebase-vaiheessa (vko 6)
            synced = false           // Merkitään synkronoimattomaksi
        )
        dao.insert(spotWithUser)
    }

    /** Poistaa luontolöydön paikallisesta tietokannasta. */
    suspend fun deleteSpot(spot: NatureSpot) {
        dao.delete(spot)
    }

    /** Hakee synkronoimattomat löydöt (käytetään myöhemmin Firebase-vaiheessa) */
    suspend fun getUnsyncedSpots(): List<NatureSpot> {
        return dao.getUnsyncedSpots()
    }
}
