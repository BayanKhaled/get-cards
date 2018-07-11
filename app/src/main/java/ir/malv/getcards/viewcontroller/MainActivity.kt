package ir.malv.getcards.viewcontroller

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import ir.malv.getcards.R
import ir.malv.getcards.Tools
import ir.malv.getcards.Tools.Companion.askPermission
import ir.malv.getcards.Tools.Companion.playSound
import ir.malv.getcards.Tools.Companion.toast
import ir.malv.getcards.model.Cards
import ir.malv.getcards.model.FlipAnimation
import ir.malv.getcards.network.Network
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_card.*


/**
 * Story of this page is:
 *  First it asks you to allow permissions needed.
 *  If you deny nothing will be done! so it is needed.
 *  else if you allowed it quickly
 */
class MainActivity : AppCompatActivity() {

    companion object {
        var cards: Cards? = null
        var mediaPlayer: MediaPlayer? = MediaPlayer()
        var isRequesting = false
        var index = -1
    }

    /**
     * In onCreate only permission gets checked.
     * Result of permission will be sent and handled in another function
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = "Get The Cards"
        toolbar.subtitle = "Version 0.1"
        //ask for the permission with explanation every time app starts
        getPermission()
    }

    /**
     * To avoid duplication asking is a function now
     */
    private fun getPermission() {
        isRequesting = true
        progressBar.visibility = View.VISIBLE
        askPermission(this, "Vibration needed.",
                "Some cards might vibrate your device, So we need this permission. If you deny app won't function.",
                this::handlePermissionResult, Manifest.permission.VIBRATE
        )
    }


    /**
     * In here if permission is granted
     * method reference redirects to two different methods
     * for data success @see #onDataReceived
     * for failure @see #onDataFailed
     * if permission denied nothing will function
     */
    private fun handlePermissionResult(result: Boolean) {
        if (result) {
            //Use method reference
            println("permission is GRANTED")
            if (cards == null)
                Network.getData(this::onDataReceived, this::onDataFailed)
            else
                fillCards()
        } else {

            //TODO: you can show a layout for this situation
            toast(this, "No functionality when permission is denied.")
        }
    }

    /**
     * If data received data will be passed to @see #onDataReceived
     * It uses try-catch to wrap #onDataReceived
     */
    private fun onDataReceived(data: String) {
        try {
            useDataWithCaution(data)
        } catch (e: Exception) {
            toast(this, "Failed to handle received data. Structure might have changed!")
        }
    }

    /**
     * USED by @see #useDataWithCaution
     * @param data is the data returned
     *
     */
    private fun useDataWithCaution(data: String) {
        //Use GSON to extract data and fill cards
        println("Data is $data")
        cards = Gson().fromJson<Cards>(data, Cards::class.java)
        fillCards()
    }

    /**
     * Use cards to fill CardView
     */
    @SuppressLint("SetTextI18n")
    private fun fillCards() {
        isRequesting = false
        progressBar.visibility = View.GONE
        index = Tools.getRandomNumberInRange(0, cards!!.cards.size - 1)
        val item = cards!!.cards[index]
        animateFab(card)
        with(item) {
            titleText.text = title
            descriptionText.text = description
            tagText.text = "$tag ($code)"
        }
        if (mediaPlayer != null)
            mediaPlayer!!.stop()
        mediaPlayer = null
        mediaPlayer = MediaPlayer()
        when (item.code) {
            0 -> Picasso.get().load(item.image).into(image).also { image.visibility = View.VISIBLE }
            1 -> Tools.vibrate(this).also { image.visibility = View.INVISIBLE }
            2 -> Tools.playSound(this, item.sound, mediaPlayer!!).also {
                toast(this, "Click on toolbar to stop media.")
                image.visibility = View.INVISIBLE
            }
        }
    }

    private fun animateFab(v: View) {
        val flipAnimation = FlipAnimation(v, v)
        if (v.visibility == View.GONE) {
            flipAnimation.reverse()
        } else {
            v.startAnimation(flipAnimation)
        }
    }

    /**
     * USED by @see #handlePermissionResult
     * If network request failed to get data
     * It will show a dialog with error
     * @param t is the throwable holding the error message
     */
    private fun onDataFailed(t: Throwable) {
        println("DATA failed")
        //print error message and aware user
        Tools.alert(this, "Failed to get data", "Error:\n ${t.message}")
    }

    /**
     * If the retry fab is clicked this will be invoked
     */
    fun onFabClick(view: View) {
        if (!isRequesting) {
            getPermission()
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) mediaPlayer?.stop()

        } else
            toast(this, "Hold On!")
    }

    fun onClickToolbar(view: View) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) mediaPlayer?.pause()
    }


    /**
     * If app is closed mediaPlayer must be stopped
     */
    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (index == 2 && cards != null) {
            mediaPlayer = MediaPlayer()
            playSound(this, cards!!.cards[index].sound, mediaPlayer!!)
        }
    }
}
