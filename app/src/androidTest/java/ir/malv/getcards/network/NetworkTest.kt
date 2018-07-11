package ir.malv.getcards.network

import android.support.test.runner.AndroidJUnit4
import android.support.test.runner.AndroidJUnitRunner
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ir.malv.getcards.model.Cards as CardsClass
import retrofit2.converter.scalars.ScalarsConverterFactory

@RunWith(AndroidJUnit4::class)
class NetworkTest {

    /**
     * Test getting data from webservice
     * Use retrofit to test data received
     */
    @Test
    fun getData() {
        val url = "http://static.pushe.co/"
        val retrofitNormal = Retrofit.Builder().baseUrl(url).addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        val req = retrofitNormal.create(Cards::class.java)
        req.getCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val data : CardsClass = Gson().fromJson<CardsClass>(it, CardsClass::class.java)
                    assertNotNull(data)
                    val number = data.cards.size
                    assertEquals(number, 5)
                }, {
                    assertNull(it)
                })
    }
}