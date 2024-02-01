
package rate.spotapp

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class Track(
    @SerializedName("name") var name: String = "Unknown Track",
    @SerializedName("artists") var artists: Array<Artist> = emptyArray(),
    @SerializedName("album") var album: Album = Album(),
    @SerializedName("duration_ms") var musicDuration_ms: Int = 180000,
    //for our DB
    @SerializedName("id") val id: String,
    var times: Int = 1,
    var rating: Int? = null,
    //from album
    var imageUrl: String? = null,
) :
    Parcelable {

    // Secondary constructor for creating a Track with minimal information
    constructor(
        id: String,
        times: Int,
        rating: Int?
    ) : this("Unknown Track", emptyArray(), Album("","Unknown Album", emptyList()), 180000, id, times, rating, null)

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "Unknown Track",
        parcel.createTypedArray(Artist) ?: emptyArray(),
        parcel.readParcelable(Album::class.java.classLoader) ?: Album("","Unknown Album", emptyList()),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeTypedArray(artists, flags)
        parcel.writeParcelable(album, flags)
        parcel.writeInt(musicDuration_ms)
        parcel.writeString(id)
        parcel.writeInt(times)
        parcel.writeValue(rating)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}

data class ListPlayedTrack(
    @SerializedName("items") val items: List<PlayedTrack>
)

data class PlayedTrack(
    @SerializedName("track") val track: Track,
    @SerializedName("played_at") var date: String
) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Track::class.java.classLoader)!!,
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(track, flags)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayedTrack> {
        override fun createFromParcel(parcel: Parcel): PlayedTrack {
            return PlayedTrack(parcel)
        }

        override fun newArray(size: Int): Array<PlayedTrack?> {
            return arrayOfNulls(size)
        }
    }
}


data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String = "Unknown Artist",
    @SerializedName("genres") var genres: List<String>,
    @SerializedName("followers") var followers: FollowersInfo,
    @SerializedName("images") var imageUrl: List<ImageObject>,
) :
    Parcelable {
    // Secondary constructor for creating a empty Album
    constructor(
    ) : this("","Unknown Artist", emptyList(), FollowersInfo(0), emptyList())

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readParcelable(FollowersInfo::class.java.classLoader) ?: FollowersInfo(0),
        parcel.createTypedArrayList(ImageObject) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeStringList(genres)
        parcel.writeParcelable(followers, flags)
        parcel.writeTypedList(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist {
            return Artist(parcel)
        }

        override fun newArray(size: Int): Array<Artist?> {
            return arrayOfNulls(size)
        }
    }
}
data class FollowersInfo(
    @SerializedName("total") val total: Int = 0
) :
    Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(total)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FollowersInfo> {
        override fun createFromParcel(parcel: Parcel): FollowersInfo {
            return FollowersInfo(parcel)
        }

        override fun newArray(size: Int): Array<FollowersInfo?> {
            return arrayOfNulls(size)
        }
    }
}


data class Album(
    @SerializedName("id") val id: String = "Unknown Album",
    @SerializedName("name") val name: String = "",
    @SerializedName("images") val imageUrl: List<ImageObject>,
    var timeListening: Int = 0,
    var date: String = "",
    var avgRate: Int? = null
) :
    Parcelable {

    // Secondary constructor for creating a empty Album
    constructor(
    ) : this("","Unknown Album", emptyList())


    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(ImageObject) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeTypedList(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}

data class ImageObject(
    @SerializedName("url") val url: String
) :
    Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageObject> {
        override fun createFromParcel(parcel: Parcel): ImageObject {
            return ImageObject(parcel)
        }

        override fun newArray(size: Int): Array<ImageObject?> {
            return arrayOfNulls(size)
        }
    }
}


data class UserProfile(
    @SerializedName("id") val id: String
)
