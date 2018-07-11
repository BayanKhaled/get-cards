package ir.malv.getcards.network

import io.reactivex.Single
import ir.malv.network.provider.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import ir.malv.network.provider.Async.Companion.async

class Network {

    companion object {
        const val url = "http://static.pushe.co/"

        fun getData(onSuccess: (String)->Unit, onFailed: (Throwable)->Unit) {
            val retrofit = Retrofit.getRx(url, ScalarsConverterFactory.create())
            val request = retrofit.create(Cards::class.java)
            request.getCards().async().subscribe(onSuccess, onFailed)
        }
    }
}

//Simple request
interface Cards {
    @GET("challenge/json")
    fun getCards(): Single<String>
}