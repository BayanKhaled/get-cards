package ir.malv.getcards

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.tedpark.tedpermission.rx2.TedRx2Permission
import ir.malv.getcards.viewcontroller.MainActivity
import ir.malv.getcards.viewcontroller.MainActivity.Companion.cards
import java.util.*

/**
 * Gives access to the features that don't belong to view controller
 */
class Tools {

    companion object {

        /**
         * Vibrates the device
         * @param c is the context of the Context asking for vibration
         * @param duration is the duration to vibrate (default is 500 ms)
         */
        fun vibrate(c: Context, duration: Long = 500) {
            val v = c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            else
                v.vibrate(duration) //deprecated in API 26
        }

        /**
         * <b>Remember this need one dependency code</b>
         * Code: implementation 'gun0912.ted:tedpermission-rx2:2.2.0' // Or later
         * Might also need: implementation 'io.reactivex.rxjava2:rxandroid:2.0.2' //Or later
         * @param c is the Context
         * @param deniedTitle is the Title when permission denied and you are showing a message that you need it
         * @param deniedMessage is the Message
         * @param doWithPermissionResult is a function that has a Boolean. If true the permission is granted
         * @param permissions are the Permission list like <code>Manifest.permission.READ_CONTACTS</code>
         * {@see https://github.com/ParkSangGwon/TedPermission}
         * <b>Remember it's not a long method it's just one line actually</b>
         */
        fun askPermission(c: Context,
                          deniedTitle: String,
                          deniedMessage: String,
                          doWithPermissionResult: (Boolean) -> Unit,
                          vararg permissions: String
        ) =
                TedRx2Permission.with(c)
                        .setDeniedTitle(deniedTitle)
                        .setDeniedMessage(deniedMessage)
                        .setPermissions(*permissions)
                        .request()
                        .subscribe({
                            doWithPermissionResult(it.isGranted)
                        }, {
                            System.err.println("Error in permission: ${it.message}")
                        })!!

        /**
         * Generates a random number between the range
         * @throws IllegalArgumentException if max is less than or equal to min
         * @return a random number between range
         */
        fun getRandomNumberInRange(min: Int, max: Int): Int {
            if (min >= max) throw IllegalArgumentException("max must be greater than min")
            return Random().nextInt(max - min + 1) + min
        }

        fun toast(c: Context, s: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(c, s, duration).show()

        fun alert(c: Context, title: String, message: String) =
                AlertDialog.Builder(c)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("Ok", null)
                        .create().show()

        fun playSound(c: Context, url: String, m: MediaPlayer, onPrepared: ()->Unit) {
            try {
                m.let {
                    if (it.isPlaying) it.reset()
                    else {
                        it.setDataSource(c, Uri.parse(url))
                        it.prepareAsync()
                        it.setOnPreparedListener {
                            onPrepared()
                            it.start() }
                    }
                }
            } catch (e: Exception) {
                System.err.println("ERROR_PLAYING MEDIA ${e.message}")
            }
        }
    }
}