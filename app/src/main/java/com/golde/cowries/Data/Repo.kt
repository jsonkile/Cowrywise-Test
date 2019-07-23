package com.golde.cowries.Data

import android.util.Log
import androidx.annotation.WorkerThread
import com.golde.cowries.Network.FixerApi
import com.golde.cowries.Util.DateTimeUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class Repo() {
    val fixerApi by lazy {
        FixerApi.create()
    }
    val rates  = Realm.getDefaultInstance().where(Rates::class.java).findAll()
    val TAG = "Repo report"
    var cpSuccess : Boolean = false
    var dates : Array<String> = arrayOf()
    var checkpoints : MutableMap<String, Double> = mutableMapOf<String, Double>()

    suspend fun downloadRatesAndUpdateRealm() {
        try {
            if(fixerApi.getRatesAsync().isSuccessful){
                val rates = Gson().fromJson(fixerApi.getRatesAsync().body(), JsonObject::class.java).getAsJsonObject("rates")
                Log.d(TAG,"Data downloaded Successfully!")
                writeToRealm(rates)
            }else{
                Log.d(TAG,"Data download failed!")
            }
        }catch (e : IOException){
        }finally {
            Log.d(TAG,"Data download failed! (Check network)")
        }
    }

    fun returnRates() : RealmResults<Rates> {
        return rates
    }

    private fun writeToRealm(rates: JsonObject){
        Realm.getDefaultInstance().executeTransaction {
            for (t in rates.keySet()){
                val rate = it.createObject(Rates::class.java)
                rate.key = t.toString()
                rate.rate = rates.get(t).toString().toDouble()
            }
        }
    }

    suspend fun History30Days(symbols : Array<String>) : Map<String, Double>{
        cpSuccess = false
        GlobalScope.launch {
            try {
                Log.d("DateTimez =>", DateTimeUtil.format(DateTime.now() - 6.days))
                val six = fixerApi.getHistoricalRatesAsync(DateTimeUtil.format(DateTime.now() - 6.days), "${symbols[0]},${symbols[1]}")
                val twelve = fixerApi.getHistoricalRatesAsync(DateTimeUtil.format(DateTime.now() - 12.days), "${symbols[0]},${symbols[1]}")
                val ayteen = fixerApi.getHistoricalRatesAsync(DateTimeUtil.format(DateTime.now() - 18.days), "${symbols[0]},${symbols[1]}")
                val twenti4 = fixerApi.getHistoricalRatesAsync(DateTimeUtil.format(DateTime.now() - 24.days), "${symbols[0]},${symbols[1]}")
                val tety = fixerApi.getHistoricalRatesAsync(DateTimeUtil.format(DateTime.now() - 30.days), "${symbols[0]},${symbols[1]}")
                val dates = arrayOf(DateTimeUtil.format2(DateTime.now() - 6.days), DateTimeUtil.format2(DateTime.now() - 12.days),DateTimeUtil.format2(DateTime.now() - 18.days),DateTimeUtil.format2(DateTime.now() - 24.days),DateTimeUtil.format2(DateTime.now() - 30.days))
                this@Repo.dates = dates
                successHandler(six.body(), twelve.body(), ayteen.body(), twenti4.body(), tety.body(), symbols)
            } catch (exception: IOException) {
                Log.e("TAG", exception.message)
                cpSuccess = false
            }
        }

        return checkpoints
    }

    private fun successHandler(body1: String?, body2: String?, body3: String?, body4: String?, body5: String?, symbols : Array<String>) : Map<String, Double>{
        val b1 = offerRate(Gson().fromJson(body1, JsonObject::class.java).getAsJsonObject("rates"), symbols)
        val b2 = offerRate(Gson().fromJson(body2, JsonObject::class.java).getAsJsonObject("rates"),symbols)
        val b3 = offerRate(Gson().fromJson(body3, JsonObject::class.java).getAsJsonObject("rates"), symbols)
        val b4 = offerRate(Gson().fromJson(body4, JsonObject::class.java).getAsJsonObject("rates"), symbols)
        val b5 = offerRate(Gson().fromJson(body5, JsonObject::class.java).getAsJsonObject("rates"), symbols)
        checkpoints.put(Gson().fromJson(body1, JsonObject::class.java).get("date").asString, b1)
        checkpoints.put(Gson().fromJson(body2, JsonObject::class.java).get("date").asString, b2)
        checkpoints.put(Gson().fromJson(body3, JsonObject::class.java).get("date").asString, b3)
        checkpoints.put(Gson().fromJson(body4, JsonObject::class.java).get("date").asString, b4)
        checkpoints.put(Gson().fromJson(body5, JsonObject::class.java).get("date").asString, b5)
        cpSuccess = true
        return checkpoints
    }

    private fun offerRate(jo : JsonObject, symbols : Array<String>) : Double {
        Log.d("gfgfg", symbols[0])
        val eur2FromRate = jo.get(symbols[0]).toString().toDouble()
        val eur2ToRate = jo.get(symbols[1]).toString().toDouble()
        return (1 / eur2FromRate) / (1 / eur2ToRate)
    }
}