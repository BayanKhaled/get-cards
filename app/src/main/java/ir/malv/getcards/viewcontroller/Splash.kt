package ir.malv.getcards.viewcontroller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import ir.malv.getcards.R
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * The story of this page is:
 *  when it starts it animates an image for 1.5 sec and then starts next page after animating ended.
 */
class Splash : AppCompatActivity() {

    /**
     * Only one thing happens... Animates a view
     * @param savedInstanceState is useless in this case
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setSupportActionBar(toolbar)
        animateView(imageView, this::afterAnimating)
    }

    /**
     * start an activity after animation stops
     */
    private fun afterAnimating() = startActivity(Intent(this, MainActivity::class.java)).also { finish() }

    /**
     * animate view
     * @param v is the View to animate for 1.5 sec
     * @param onEnd is the function thaT will be invoked after animation ended.
     */
    private fun animateView(v: View, onEnd: () -> Unit) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_icon)
        animation.duration = 1500
        animation.setAnimationListener(object: Listener() {
            override fun onAnimationEnd(animation: Animation?) = onEnd()
        })
        v.startAnimation(animation)
    }

    /**
     * Simple wrapper class to avoid extra implementing for animation listener
     * This is way to do it.
     */
    abstract inner class Listener: Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) = println("Animation repeated")

        override fun onAnimationStart(animation: Animation?) = println("Animation started")

    }
}
