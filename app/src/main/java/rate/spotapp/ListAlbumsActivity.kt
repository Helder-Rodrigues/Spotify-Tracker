package rate.spotapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ListAlbumsActivity: AppCompatActivity() {
    private var accessToken: String? = null
    private lateinit var tracks : List<PlayedTrack>
    private lateinit var listView: ListView
    private lateinit var spotifyClient : SpotifyClient
    private var userId: String? = null
    private lateinit var databaseClient: DatabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        // Retrieve access token from the intent extras
        intent.extras?.let {
            accessToken = it.getString(MainActivity.ACCESS_TOKEN)
            tracks = it.getParcelableArrayList<PlayedTrack>(MainActivity.TRACKS)?.toList() ?: emptyList()
            userId = it.getString(MainActivity.USERID)
        }

        spotifyClient = SpotifyClient(accessToken.toString())
        databaseClient = DatabaseClient(userId!!)

        // Initialize ListView
        listView = findViewById(R.id.AlbumList)

        val albums = groupTracksByAlbum(tracks)

        // Create an ArrayAdapter to populate the ListView
        val adapter = TrackAdapter(this, albums)

        // Set the adapter for the ListView
        listView.adapter = adapter
    }

    private fun groupTracksByAlbum(tracks : List<PlayedTrack>) : List<Album> {
        // Group tracks by Album ID
        val idToTracksMap = mutableMapOf<String, MutableList<PlayedTrack>>()
        for (track in tracks) {
            if (track.track.album.id != "") {
                val id = track.track.album.id
                idToTracksMap.computeIfAbsent(id) { mutableListOf() }.add(track)
            }
        }

        // define the albums
        val albums = mutableListOf<Album>()
        for ((_, tracksList) in idToTracksMap) {
            //start with a base, already with name and image
            val album = tracksList.first().track.album

            if (tracksList.size > 1) {
                //total time Listening
                album.timeListening =
                    tracksList.sumOf { it.track.musicDuration_ms * it.track.times }

                //find the most recent track listened of the album
                val mostRecentTrack = tracksList.maxByOrNull {parseDate(it.date)}
                if (mostRecentTrack != null)
                    album.date = mostRecentTrack.date

                // calculate the average rating of the album
                if (tracksList.any { it.track.rating != null })
                    album.avgRate = tracksList
                        .filter { it.track.rating != null }
                        .map { it.track.rating!! } // Use !! to assert that rating is non-null
                        .average()
                        .roundToInt()
                else
                    album.avgRate = null

            }
            else{
                val track = tracksList.first()
                album.timeListening = track.track.musicDuration_ms * track.track.times
                album.date = tracksList.first().date
                album.avgRate = track.track.rating
            }

            albums.add(album)
        }

        return albums
    }

    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return dateFormat.parse(dateString)
    }

    inner class TrackAdapter(context: Context, private val albums: List<Album>) : ArrayAdapter<Album>(context, 0, albums) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null)
                itemView = LayoutInflater.from(context).inflate(R.layout.row_album, parent, false)

            val album = getItem(position)

            // Bind data to the views in the custom layout
            //Image
            val imageViewAlbum: ImageView = itemView!!.findViewById(R.id.imageView2)
            Picasso.get().load(album!!.imageUrl.first().url).into(imageViewAlbum)

            //Name
            val txtViewName : TextView = itemView.findViewById(R.id.textViewNameAlbum)
            txtViewName.text = album.name

            //Minutes Listened
            val txtViewMinutes : TextView = itemView.findViewById(R.id.textViewMinutesAlbum)
            txtViewMinutes.text = "Time Listened: ${formatDuration(album.timeListening.toLong())}"

            //LastDateListened
            val txtViewDateListened : TextView = itemView.findViewById(R.id.textViewLastDateListened)
            txtViewDateListened.text = "Last Listen: ${formatDate(album.date)}"

            val textViewRating : TextView = itemView.findViewById(R.id.textViewRating)
            textViewRating.text = album.avgRate?.toString() ?: "x"
            setStarRating(album.avgRate ?: 0, itemView)



            /*itemView.setOnClickListener{
                val intent = Intent(this@ListAlbumsActivity, ListSongActivity::class.java)
                intent.putExtra(MainActivity.ACCESS_TOKEN, accessToken)
                startActivity(intent)
            }*/

            return itemView!!
        }

        private fun setStarRating(avgRate: Int, itemView: View){
            var stars = avgRate / 2
            val halfStar = (avgRate % 2 == 1)


            for (i in 1..stars) {
                val starId = resources.getIdentifier("imageViewStar$i", "id", packageName)
                val star: ImageView = itemView.findViewById(starId)
                star.setImageResource(R.drawable.baseline_star_rate_24)
            }
            if (halfStar){
                stars = stars+1
                val starId = resources.getIdentifier("imageViewStar$stars", "id", packageName)
                val star: ImageView = itemView.findViewById(starId)
                star.setImageResource(R.drawable.baseline_star_half_24)
            }
            for (i in (stars+1)..5){
                val starId = resources.getIdentifier("imageViewStar$i", "id", packageName)
                val star: ImageView = itemView.findViewById(starId)
                star.setImageResource(R.drawable.baseline_star_border_24)
            }
            Thread.sleep(10)
        }

        // Helper function to format duration in milliseconds to a readable format
        private fun formatDuration(durationMs: Long): String {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }

        fun formatDate(inputDate: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            try {
                val date = inputFormat.parse(inputDate)
                return outputFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return ""
        }
    }
}