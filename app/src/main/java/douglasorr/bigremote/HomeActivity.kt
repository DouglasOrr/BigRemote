package douglasorr.bigremote

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote


class HomeActivity : AppCompatActivity() {
    private var currentSpotifyConnection: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.home_button_skip).setOnClickListener{ skip() }
    }

    private fun skip() {
        Log.d("User", "Skip")
        currentSpotifyConnection?.playerApi?.skipNext()?.apply {
            setResultCallback { Log.d("Spotify", "Skip success") }
            setErrorCallback { Log.d("Spotify", "Skip error ${it.message}") }
        }
    }

    override fun onResume() {
        super.onResume()

        val connectionParams = ConnectionParams.Builder("358462dd3a7a4d92b910e67262c921b6")
            .setRedirectUri("douglasorr.bigremote://callback")
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object: Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote?) {
                    Log.d("Spotify", "Connected: $spotifyAppRemote")
                    currentSpotifyConnection = spotifyAppRemote
                }
                override fun onFailure(error: Throwable?) {
                    Log.d("Spotify", "Error - not connected: ${error?.message}")
                    currentSpotifyConnection = null
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        SpotifyAppRemote.disconnect(currentSpotifyConnection)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("User", "onKeyDown $keyCode ${event?.unicodeChar}")

        if (keyCode in 19..22) {
            skip()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
