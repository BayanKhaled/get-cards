package ir.malv.getcards

import android.Manifest
import android.content.Context
import android.os.Vibrator
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.tedpark.tedpermission.rx2.TedRx2Permission
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolsTestAndroid {

    lateinit var appContext: Context

    @Before
    fun getReady() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getTargetContext()
    }
    /**
     * Test that we have access to vibrator
     */
    @Test
    fun vibrate() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        assertNotNull(vibrator)
    }

    @Test
    fun permissionTest() {
        TedRx2Permission.with(appContext)
                .setDeniedTitle("Title")
                .setDeniedMessage("Message")
                .setPermissions(Manifest.permission.INTERNET) // Gives access without runtime permission
                .request()
                .subscribe({
                    assertTrue(it.isGranted)
                }, {
                    assertNull(it)
                })!!
    }

    @After
    fun done() {
        println("Tests done")
    }
}