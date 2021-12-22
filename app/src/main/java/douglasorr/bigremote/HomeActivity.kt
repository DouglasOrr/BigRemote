package douglasorr.bigremote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp


class HomeActivity : AppCompatActivity() {
    private var currentSpotifyConnection: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.home_button_skip).setOnClickListener{ skip() }
        findViewById<Button>(R.id.home_button_download).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
            })
        }
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
                    currentSpotifyConnection = spotifyAppRemote
                    findViewById<View>(R.id.home_layout_main).visibility = View.VISIBLE
                    findViewById<View>(R.id.home_button_download).visibility = View.GONE
                    findViewById<View>(R.id.home_text_error).visibility = View.GONE
                }
                override fun onFailure(error: Throwable?) {
                    Log.d("Spotify", "Error - not connected: ${error?.javaClass?.name} ${error?.message}")
                    currentSpotifyConnection = null
                    if (error is CouldNotFindSpotifyApp) {
                        findViewById<View>(R.id.home_layout_main).visibility = View.GONE
                        findViewById<View>(R.id.home_button_download).visibility = View.VISIBLE
                        findViewById<View>(R.id.home_text_error).visibility = View.GONE
                    } else {
                        findViewById<View>(R.id.home_layout_main).visibility = View.GONE
                        findViewById<View>(R.id.home_button_download).visibility = View.GONE
                        findViewById<TextView>(R.id.home_text_error).apply {
                            visibility = View.VISIBLE
                            text = "Spotify connection error: ${error?.javaClass?.simpleName}"
                        }
                    }
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
