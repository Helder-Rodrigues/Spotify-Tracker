package rate.spotapp

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DatabaseClient(private val userId: String) {
    private val userTracksReference =
        FirebaseDatabase.getInstance().getReference(Config.databaseTracksRefTemplate.format(userId))

    fun fetchTracksFromDatabase(callback: (List<PlayedTrack>) -> Unit) {
        val additionalTracks = mutableListOf<PlayedTrack>()

        userTracksReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()){
                    for (trackSnapshot in snapshot.children) {
                        val trackId = trackSnapshot.key.toString()
                        val trackTimes = (trackSnapshot.child("times").value as? Long)?.toInt() ?: 0
                        val trackRating = (trackSnapshot.child("rating").value as? Long)?.toInt()
                        val trackDate = trackSnapshot.child("date").value as String

                        // Create a PlayedTrack object from the fetched data
                        val playedTrack = PlayedTrack(
                            Track(id = trackId, times = trackTimes, rating = trackRating),
                            date = trackDate
                        )

                        additionalTracks.add(playedTrack)
                    }
                }
                callback(additionalTracks)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e("API_RESPONSE", "Error Response: ${error.message}")
            }
        })
    }

    //region Save Tracks Functions
    fun saveTrackList(listTrackItem: List<PlayedTrack>){
        for (trackItem in listTrackItem)
            saveTrack(trackItem)
    }

    fun saveTrack(trackItem: PlayedTrack){
        val trackReference = userTracksReference.child(trackItem.track.id)
        trackReference.child("id").setValue(trackItem.track.id)
        trackReference.child("times").setValue(trackItem.track.times)
        trackReference.child("rating").setValue(trackItem.track.rating)
        trackReference.child("date").setValue(trackItem.date)
    }
    //endregion
}