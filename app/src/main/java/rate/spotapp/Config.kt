package rate.spotapp

import io.github.cdimascio.dotenv.dotenv

object Config {
    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env"
    }

    //spotify
    val spotifyClientId: String = dotenv["SPOTIFY_CLIENT_ID"] ?: ""
    val spotifyRedirectUri: String = dotenv["SPOTIFY_REDIRECT_URI"] ?: ""

    //database
    val databaseTracksRefTemplate: String = dotenv["DATABASE_TRACKS_REFERENCE_TEMPLATE"] ?: ""
}