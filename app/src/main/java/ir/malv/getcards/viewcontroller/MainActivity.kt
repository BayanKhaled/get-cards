package ir.malv.getcards.viewcontroller

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.AppCompatButton
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

typealias LP = LinearLayout.LayoutParams

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
        //Add support for SVG
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        title = "Get The Cards"
        toolbar.subtitle = "Version 0.3"
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
     * if permission denied nothing will function and a layout will be shown
     * -> ! If you wanna see that layout just reverse result value by changing the code
     * from `result` to `!result`
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
            setContentView(getDeniedPermissionLayout())
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
        when (item.code) {
            0 -> Picasso.get().load(item.image).into(image).also { image.visibility = View.VISIBLE }
            1 -> Tools.vibrate(this).also { image.visibility = View.INVISIBLE }
            2 -> Tools.playSound(this, item.sound, mediaPlayer!!, { progressBar.visibility = View.GONE }).also {
                toast(this, "Click on toolbar to stop media.")
                image.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
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
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) mediaPlayer!!.stop()

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
            playSound(this, cards!!.cards[index].sound, mediaPlayer!!, { progressBar.visibility = View.GONE})
            progressBar.visibility = View.VISIBLE
        }
    }


    /**
     * If permission is denied this function makes a layout
     * to show and give option to retry
     * !!! This'll be useless... Unless you take back permission.
     * Usually permission is granted.
     * -> Bad habit : Long method
     */
    private fun getDeniedPermissionLayout(): View {
        val layoutContainer = LinearLayout(this)
        layoutContainer.layoutParams = LP(LP.MATCH_PARENT, LP.MATCH_PARENT)
        layoutContainer.orientation = LinearLayout.VERTICAL
        layoutContainer.gravity = Gravity.CENTER
        layoutContainer.setBackgroundColor(resources.getColor(R.color.colorAccent))
        //Image
        val image = ImageView(this)
        image.setImageResource(R.drawable.ic_no)
        image.layoutParams = LP(120, 100)
        //Text
        val text = TextView(this)
        text.text = "Sorry, Without permission we can't do anything :("
        text.layoutParams = LP(LP.MATCH_PARENT, LP.WRAP_CONTENT)
        text.textSize = 18f // 18 sp
        text.gravity = Gravity.CENTER
        text.setTextColor(Color.parseColor("#FFFFFF"))
        //Button
        val retry = AppCompatButton(this)
        retry.layoutParams = LP(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
        retry.text = "Retry"
        retry.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retry, 0, 0, 0)
        retry.setTextColor(Color.parseColor("#FFFFFF"))
        ViewCompat.setBackgroundTintList(retry, ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
        retry.setOnClickListener { recreate() }
        //Add them to container
        layoutContainer.addView(image)
        layoutContainer.addView(text)
        layoutContainer.addView(retry)
        return layoutContainer
    }
}
