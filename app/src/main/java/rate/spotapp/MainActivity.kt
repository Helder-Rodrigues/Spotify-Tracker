package rate.spotapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private val clientId = Config.spotifyClientId
    private val redirectUri = Config.spotifyRedirectUri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log in
        val authorizationUri = buildAuthorizationUri()
        val browserIntent = Intent(Intent.ACTION_VIEW, authorizationUri)
        startActivity(browserIntent)
    }

    private fun buildAuthorizationUri(): Uri {
        val baseUri = Uri.parse("https://accounts.spotify.com/authorize")

        val uriBuilder = baseUri.buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", "user-read-recently-played streaming")

        return uriBuilder.build()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.data?.let { uri ->
            // Parse the URI and handle the authorization response
            handleAuthorizationResponse(uri)
        }
    }

    private fun handleAuthorizationResponse(uri: Uri) {
        val fragment = uri.fragment

        val accessToken = Uri.parse("?$fragment").getQueryParameter("access_token")

        if (!accessToken.isNullOrBlank()) {
            val intent = Intent(this, ListSongActivity::class.java)
            intent.putExtra(ACCESS_TOKEN, accessToken)
            startActivity(intent)
        } else {
            Log.d("Error", "access_token: $accessToken")
        }
    }


    companion object {
        const val  ACCESS_TOKEN = "access_token"
        const val  TRACKS = "tracks"
        const val  USERID = "user_id"
    }
}