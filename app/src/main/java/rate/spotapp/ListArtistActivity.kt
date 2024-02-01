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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.roundToInt


class ListArtistActivity: AppCompatActivity() {
    private var accessToken: String? = null
    private lateinit var tracks : List<PlayedTrack>
    private lateinit var listView: ListView
    private lateinit var spotifyClient : SpotifyClient
    private var userId: String? = null
    private lateinit var databaseClient: DatabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)

        // Retrieve access token from the intent extras
        intent.extras?.let {
            accessToken = it.getString(MainActivity.ACCESS_TOKEN)
            tracks = it.getParcelableArrayList<PlayedTrack>(MainActivity.TRACKS)?.toList() ?: emptyList()
            userId = it.getString(MainActivity.USERID)
        }

        spotifyClient = SpotifyClient(accessToken.toString())
        databaseClient = DatabaseClient(userId!!)

        // Initialize ListView
        listView = findViewById(R.id.ArtistList)

        val artists = getAllArtists(tracks)

        if (!artists.isNullOrEmpty()){
            spotifyClient.fetchAllArtistDetails(artists) { updatedArtistList ->
                // Create an ArrayAdapter to populate the ListView
                val adapter = TrackAdapter(this, updatedArtistList)

                // Set the adapter for the ListView
                listView.adapter = adapter
            }
        }
    }

    private fun getAllArtists(tracks: List<PlayedTrack>): List<Artist> {
        val uniqueArtists = mutableSetOf<Artist>()

        for (track in tracks) {
            val artists = track.track.artists
            uniqueArtists.addAll(artists)
        }

        return uniqueArtists.toList()
    }

    inner class TrackAdapter(context: Context, private val artists: List<Artist>) : ArrayAdapter<Artist>(context, 0, artists) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null)
                itemView = LayoutInflater.from(context).inflate(R.layout.row_artist, parent, false)

            val artist = getItem(position)

            // Bind data to the views in the custom layout
            //Image
            val imageViewArtist: ImageView = itemView!!.findViewById(R.id.imageView)
            if (artist?.imageUrl?.firstOrNull() != null)
                Picasso.get().load(artist.imageUrl.first().url).into(imageViewArtist)

            //Name
            val textViewArtistName: TextView = itemView.findViewById(R.id.textViewNameArtist)
            textViewArtistName.text = artist?.name ?: "Unknown Artist"

            //Genre
            val textViewGenre : TextView = itemView.findViewById(R.id.textViewMusicGenre)
            if(!artist?.genres.isNullOrEmpty()){
                val genres = getConcatenaredStrings(artist!!.genres)
                textViewGenre.text = "Genres: ${genres}"
            }else
                textViewGenre.text = "Genres: Not Yet Classified"

            //Followers
            val textViewFollowers : TextView = itemView.findViewById(R.id.textViewFollowers)
            val numFollowers = artist?.followers?.total.toString() ?: "0"
            textViewFollowers.text =  "Followers: ${numFollowers}"


            return itemView!!
        }


        // Helper function to concatenate strings
        private fun getConcatenaredStrings(stringList: List<String>): String {
            val strings: MutableList<String> = mutableListOf()
            for (string in stringList) {
                strings.add(string)
            }
            return strings.joinToString(", ")
        }

    }
}