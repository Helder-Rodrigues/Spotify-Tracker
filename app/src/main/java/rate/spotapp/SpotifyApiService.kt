package rate.spotapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApiService {
    //region Tracks
    @GET("me/player/recently-played?limit=50")
    fun getRecentlyPlayedTracks(@Header("Authorization") authorization: String): Call<ListPlayedTrack>

    @GET("tracks/{id}")
    fun getTrackDetails(
        @Header("Authorization") authorization: String,
        @Path("id") trackId: String
    ): Call<Track>
    //endregion


    @GET("artists/{id}")
    fun getArtistDetails(
        @Header("Authorization") authorization: String,
        @Path("id") artistId: String
    ): Call<Artist>


    @GET("me")
    fun getMyProfile(@Header("Authorization") authorization: String): Call<UserProfile>
}

