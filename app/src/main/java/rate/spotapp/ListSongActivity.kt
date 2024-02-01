package rate.spotapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date

class ListSongActivity : AppCompatActivity() {
    private var accessToken: String? = null
    private lateinit var listView: ListView
    private lateinit var spotifyClient : SpotifyClient
    private var userId: String? = null
    private lateinit var databaseClient: DatabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        // Retrieve access token from the intent extras
        intent.extras?.let {
            accessToken = it.getString(MainActivity.ACCESS_TOKEN)
        }

        //define buttons
        val btnAlbums : Button = findViewById(R.id.btnAlbums)
        btnAlbums.setOnClickListener {
            val intent = Intent(this, ListAlbumsActivity::class.java)
            val tracks = getAllTracks()
            intent.putExtra(MainActivity.ACCESS_TOKEN, accessToken)
            intent.putParcelableArrayListExtra(MainActivity.TRACKS, ArrayList(tracks))
            intent.putExtra(MainActivity.USERID, userId)
            startActivity(intent)
        }

        val btnArtists : Button = findViewById(R.id.btnArtists)
        btnArtists.setOnClickListener {
            val intent = Intent(this, ListArtistActivity::class.java)
            val tracks = getAllTracks()
            intent.putExtra(MainActivity.ACCESS_TOKEN, accessToken)
            intent.putParcelableArrayListExtra(MainActivity.TRACKS, ArrayList(tracks))
            intent.putExtra(MainActivity.USERID, userId)
            startActivity(intent)
        }

        spotifyClient = SpotifyClient(accessToken.toString())
        spotifyClient.fetchUserIdFromSpotify(accessToken.toString()) { userid ->
            userId = userid
            databaseClient = DatabaseClient(userid!!)

            // Initialize ListView
            listView = findViewById(R.id.SongList)
            // Fetch recently played tracks
            spotifyClient.fetchRecentlyPlayedTracks { spotTracks ->
                // Fetch additional tracks from the database
                databaseClient.fetchTracksFromDatabase { dbTracks ->
                    handleTracks(spotTracks, dbTracks)

                    // Initialize SaveButton
                    initializeSaveButton()
                }
            }
        }
    }

    private fun getAllTracks() : List<PlayedTrack>{
        // Assuming you are using an ArrayAdapter to manage the items in the ListView
        val adapter = listView.adapter as? TrackAdapter
        return adapter?.let {
            // Access the items from the adapter
            val displayedTracks = mutableListOf<PlayedTrack>()
            for (i in 0 until it.count) {
                val track = it.getItem(i)
                if (track != null)
                    displayedTracks.add(track)
            }
            displayedTracks.toList()
        } ?: emptyList()
    }

    private fun initializeSaveButton(){
        // Initialize SaveButton
        val saveButton : Button = findViewById(R.id.btnSaveTrack)
        saveButton.setOnClickListener {
            // Iterate through the displayed tracks
            val adapter = listView.adapter as? TrackAdapter
            adapter?.let {
                // Get the entered data map from the adapter
                val enteredDataMap = it.getEnteredDataMap()

                // Iterate through the entered data map
                for ((trackId, enteredData) in enteredDataMap) {
                    val track = adapter.getTrackById(trackId)
                    if (track != null) {
                        val rate = enteredData.toIntOrNull()
                        if (rate != null)
                            track.track.rating = rate.coerceIn(0, 10)
                        else
                            track.track.rating = null
                        databaseClient.saveTrack(track)
                    }
                }
            }
        }
    }

    private fun handleTracks(spotTracks: List<PlayedTrack>, dbTracks: List<PlayedTrack>) {
        val spotTracksEmpty = spotTracks.isEmpty()
        val dbTracksEmpty = dbTracks.isEmpty()

        var onlyDbTracks = emptyList<PlayedTrack>()
        var commonTracks = emptyList<PlayedTrack>()
        var onlySpotTracks = mutableListOf<PlayedTrack>()

        if (!spotTracksEmpty && !dbTracksEmpty) {
            val (onlyDb, common) = getOnlyDbandCommonTracks(spotTracks, dbTracks)
            onlyDbTracks = onlyDb
            commonTracks = common

            // Save the changes in common tracks
            databaseClient.saveTrackList(commonTracks)

            // Grab unique tracks
            onlySpotTracks = findTracksNotInSecondList(spotTracks, commonTracks).toMutableList()

            // Merge duplicates and keep the most recent info
            keepMostRecentTracks(onlySpotTracks)

            // Save the unique tracks
            databaseClient.saveTrackList(onlySpotTracks)
        }
        else if (!spotTracksEmpty) {
            onlySpotTracks = spotTracks.toMutableList()

            // Merge duplicates and keep the most recent info
            keepMostRecentTracks(onlySpotTracks)

            // Save the unique tracks
            databaseClient.saveTrackList(onlySpotTracks)
        }
        else if (!dbTracksEmpty) {
            onlyDbTracks = dbTracks
        }

        //choose what to display
        val commonTracksEmpty = commonTracks.isNotEmpty()
        val onlySpotTracksEmpty = onlySpotTracks.isNotEmpty()

        var tracksToDisplay = emptyList<PlayedTrack>()
        if(onlyDbTracks.isNotEmpty()){
            // fetch only DB Tracks' additional details from Spotify
            spotifyClient.fetchAllTrackDetails(onlyDbTracks) { updatedOnlyDbTrackList ->
                tracksToDisplay = updatedOnlyDbTrackList
                if(commonTracksEmpty)
                    tracksToDisplay = mergeTrackLists(tracksToDisplay, commonTracks)
                if (onlySpotTracksEmpty)
                    tracksToDisplay = mergeTrackLists(tracksToDisplay, updateTrackListInfo(onlySpotTracks))
                displayTracks(tracksToDisplay)
            }
        }
        else if (onlySpotTracksEmpty){
            tracksToDisplay = updateTrackListInfo(onlySpotTracks)
            if(commonTracksEmpty)
                tracksToDisplay = mergeTrackLists(tracksToDisplay, commonTracks)
            displayTracks(tracksToDisplay)
        }
        else if (commonTracksEmpty){
            displayTracks(commonTracks)
        }
    }

    private fun displayTracks(tracks: List<PlayedTrack>) {
        // Create an ArrayAdapter to populate the ListView
        val adapter = TrackAdapter(this, tracks)

        // Set the adapter for the ListView
        listView.adapter = adapter
    }

    private fun findTracksNotInSecondList(list1: List<PlayedTrack>, list2: List<PlayedTrack>): List<PlayedTrack> {
        val idsInList2 = list2.map { it.track.id }.toSet()

        return list1.filter { track -> track.track.id !in idsInList2 }
    }

    private fun keepMostRecentTracks(tracks: MutableList<PlayedTrack>) : MutableList<PlayedTrack>{
        val idToTracksMap = mutableMapOf<String, MutableList<PlayedTrack>>()

        // Group tracks by ID
        for (track in tracks) {
            val id = track.track.id
            idToTracksMap.computeIfAbsent(id) { mutableListOf() }.add(track)
        }

        val tracksToRemove = mutableListOf<PlayedTrack>()

        // Keep the most recent track for each ID
        for ((_, tracksList) in idToTracksMap) {
            if (tracksList.size > 1){
                val mostRecentTrack = tracksList.maxByOrNull {
                    parseDate(it.date)
                }
                if (mostRecentTrack != null) {
                    // Add all tracks with the same ID as mostRecentTrack but not including mostRecentTrack itself
                    tracksToRemove.addAll(tracksList.filter { it.track.id == mostRecentTrack.track.id && it != mostRecentTrack })
                }
            }
            // Set the times value to the count of tracks with the same ID
            tracksList.first().track.times = tracksList.size
        }

        // Remove the tracks
        tracks.removeAll(tracksToRemove)

        return tracks
    }

    //endregion

    private fun getOnlyDbandCommonTracks(spotTracks: List<PlayedTrack>, dbTracks: List<PlayedTrack> ): Pair<List<PlayedTrack>, List<PlayedTrack>>  {
        val onlyDBTracks = mutableListOf<PlayedTrack>()
        val commonTracks = mutableListOf<PlayedTrack>()
        // Loop through additional tracks
        for (dbTrack in dbTracks) {
            // Check if a track with the same ID already exists in the mergedTracks list
            val existingTrack = spotTracks.filter { it.track.id == dbTrack.track.id }.toMutableList()

            if (existingTrack.isNotEmpty()) {
                // If the track exists, update info and add to the lists

                // handle times
                var times: Int = dbTrack.track.times + existingTrack.count { it.date > dbTrack.date }

                // handle date
                val mostRecentTrack = if (existingTrack.size > 1)
                    existingTrack.maxByOrNull { parseDate(it.date) }!!
                else
                    existingTrack.first()

                // update info
                mostRecentTrack.track.times = times
                mostRecentTrack.track.rating = dbTrack.track.rating
                mostRecentTrack.track.imageUrl = mostRecentTrack.track.album.imageUrl[0].url

                //add to the lists
                commonTracks.add(mostRecentTrack)
            }else{
                onlyDBTracks.add(dbTrack)
            }
        }

        return Pair(onlyDBTracks, commonTracks)
    }

    private fun mergeTrackLists(trackList1: List<PlayedTrack>, trackList2: List<PlayedTrack>): List<PlayedTrack> {
        val allTracks = mutableListOf<PlayedTrack>()

        // Add tracks from the first list
        allTracks.addAll(trackList1)

        // Update the tracks info of the second list and then add them
        allTracks.addAll(trackList2)

        return allTracks
    }

    fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return dateFormat.parse(dateString)
    }

    //region Update Tracks Functions
    private fun updateTrackListInfo(trackList: List<PlayedTrack>) : List<PlayedTrack>{
        val updatedTrackList = mutableListOf<PlayedTrack>()
        for (track in trackList)
            updatedTrackList.add(updateTrackInfo(track))
        return updatedTrackList
    }
    private fun updateTrackInfo(track: PlayedTrack) : PlayedTrack{
        track.track.imageUrl = track.track.imageUrl ?: track.track.album.imageUrl.firstOrNull()?.url
        track.track.times.takeIf { it == 0 }?.let { track.track.times = 1 }
        return track
    }
    //endregion

    inner class TrackAdapter(context: Context, private val tracks: List<PlayedTrack>) : ArrayAdapter<PlayedTrack>(context, 0, tracks) {
        // Map to store entered data for each track, using track ID as the key
        private val enteredDataMap = mutableMapOf<String, String>()

        // Function to find a track by ID in the list
        fun getTrackById(trackId: String): PlayedTrack? {
            return tracks.find { it.track.id == trackId }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.row_song, parent, false)
            }

            val track = getItem(position)

            // Bind data to the views in the custom layout
            val editTextRating: EditText = itemView!!.findViewById(R.id.editTextRatingSong)

            //Rate
            editTextRating.tag = track?.track?.id // Set track ID as a tag to identify the associated track
            editTextRating.setText(enteredDataMap[track?.track?.id] ?: track?.track?.rating?.toString() ?: "")
            // Set a text change listener to capture the entered data
            editTextRating.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val trackId = editTextRating.tag as? String
                    if (trackId != null) {
                        enteredDataMap[trackId] = s?.toString() ?: ""
                    }
                }
            })

            //Image
            val imageViewTrack: ImageView = itemView.findViewById(R.id.imageView3)
            Picasso.get().load(track!!.track.imageUrl).into(imageViewTrack)

            //Name
            val textViewTrackName: TextView = itemView.findViewById(R.id.textViewNameSong)
            textViewTrackName.text = track.track.name

            //Minutes
            val textViewTrackMinutes: TextView = itemView.findViewById(R.id.textViewMinutesSong)
            textViewTrackMinutes.text = "Minutes Listened: ${formatDuration((track.track.musicDuration_ms * track.track.times).toLong())}"

            //Times
            val textViewTrackTimes: TextView = itemView.findViewById(R.id.textViewTimesListened)
            textViewTrackTimes.text = "Times Listened: ${track.track.times}"

            return itemView
        }

        // Helper function to get entered data map
        fun getEnteredDataMap(): Map<String, String> {
            return enteredDataMap
        }

        // Helper function to format duration in milliseconds to a readable format
        private fun formatDuration(durationMs: Long): String {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }
    }

}
