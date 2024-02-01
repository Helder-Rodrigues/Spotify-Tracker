package rate.spotapp

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyClient(private val accessToken: String) {

    // Retrofit setup
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val spotifyApi = retrofit.create(SpotifyApiService::class.java)

    //region Tracks
    // Fetch recently played tracks
    fun fetchRecentlyPlayedTracks(callback: (List<PlayedTrack>) -> Unit) {
        // Make API request
        val call = spotifyApi.getRecentlyPlayedTracks("Bearer $accessToken")

        call.enqueue(object : Callback<ListPlayedTrack> {
            override fun onResponse(call: Call<ListPlayedTrack>, response: Response<ListPlayedTrack>) {
                if (response.isSuccessful) {
                    val recentlyPlayed = response.body()
                    callback(recentlyPlayed?.items ?: emptyList())
                } else {
                    // Handle the error
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_RESPONSE", "Error Response: $errorBody")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<ListPlayedTrack>, t: Throwable) {
                // Handle the failure
                Log.e("API_FAILURE", "Failure: ${t.message}")
                callback(emptyList())
            }
        })
    }

    // Function to fetch all additional track details in a Track List from Spotify
    fun fetchAllTrackDetails(trackList: List<PlayedTrack>, callback: (List<PlayedTrack>) -> Unit) {
        var fetchCounter = 0
        val updatedList = mutableListOf<PlayedTrack>()

        for (track in trackList) {
            fetchTrackDetails(track, spotifyApi) { updatedTrack ->
                updatedList.add(updatedTrack?: trackList[fetchCounter])

                // Increment the counter for each completed fetch operation
                fetchCounter++

                // Check if all tracks have been fetched
                if (fetchCounter == trackList.size)
                    callback(updatedList)
            }
        }
    }
    // Function to fetch additional track details from Spotify
    fun fetchTrackDetails(track: PlayedTrack, spotifyApi: SpotifyApiService, callback: (PlayedTrack?) -> Unit) {
        if (track != null){
            val call = spotifyApi.getTrackDetails("Bearer $accessToken", track.track.id)
            call.enqueue(object : Callback<Track> {
                override fun onResponse(call: Call<Track>, response: Response<Track>) {
                    if (response.isSuccessful) {
                        // Update track details from the Spotify response
                        val spotifyTrackDetails = response.body()
                        track.track.imageUrl = spotifyTrackDetails?.album?.imageUrl?.firstOrNull()?.url.toString()
                        track.track.name = spotifyTrackDetails?.name ?: "Name Not Found"
                        track.track.musicDuration_ms = spotifyTrackDetails?.musicDuration_ms ?: 3
                        if (track.track.times <= 0)
                            track.track.times = 1
                        track.track.album = spotifyTrackDetails?.album ?: Album()
                        track.track.artists = spotifyTrackDetails?.artists ?: emptyArray()
                        callback(track)
                    } else {
                        // Handle error response from Spotify
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_RESPONSE", "Error Response: $errorBody")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<Track>, t: Throwable) {
                    // Handle failure
                    val errorBody = t.message
                    Log.e("CALL ENQUEUE", "Error Response: $errorBody")
                    callback(null)
                }
            })
        }
    }
    //endregion

    //region Artist
    // Function to fetch all additional Artist details in a Artist List from Spotify
    fun fetchAllArtistDetails(artistList: List<Artist>, callback: (List<Artist>) -> Unit) {
        var fetchCounter = 0
        val updatedList = mutableListOf<Artist>()

        for (artist in artistList) {
            fetchArtistDetails(artist, spotifyApi) { updatedArtist ->
                updatedList.add(updatedArtist?: artistList[fetchCounter])

                // Increment the counter for each completed fetch operation
                fetchCounter++

                // Check if all tracks have been fetched
                if (fetchCounter == artistList.size)
                    callback(updatedList)
            }
        }
    }
    // Function to fetch additional Artist details from Spotify
    fun fetchArtistDetails(artist: Artist, spotifyApi: SpotifyApiService, callback: (Artist?) -> Unit) {
        if (artist != null){
            val call = spotifyApi.getArtistDetails("Bearer $accessToken", artist.id)
            call.enqueue(object : Callback<Artist> {
                override fun onResponse(call: Call<Artist>, response: Response<Artist>) {
                    if (response.isSuccessful) {
                        // Update Artist details from the Spotify response
                        val spotifyTrackDetails = response.body()
                        artist.genres = spotifyTrackDetails?.genres ?: emptyList()
                        artist.followers = spotifyTrackDetails?.followers ?: FollowersInfo(0)
                        artist.imageUrl = spotifyTrackDetails?.imageUrl ?: emptyList()

                        callback(artist)
                    } else {
                        // Handle error response from Spotify
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_RESPONSE", "Error Response: $errorBody")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<Artist>, t: Throwable) {
                    // Handle failure
                    val errorBody = t.message
                    Log.e("CALL ENQUEUE", "Error Response: $errorBody")
                    callback(null)
                }
            })
        }
    }
    //endregion

    //fetchUserId
    fun fetchUserIdFromSpotify(accessToken: String, callback: (String?) -> Unit) {
        val call = spotifyApi.getMyProfile("Bearer $accessToken")

        call.enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    val userId = userProfile?.id
                    callback(userId)
                } else {
                    // Handle error response
                    callback(null)
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                // Handle failure
                callback(null)
            }
        })
    }
}
