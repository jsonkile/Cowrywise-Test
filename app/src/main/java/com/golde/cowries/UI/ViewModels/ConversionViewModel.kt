package com.golde.cowries.UI.ViewModels

import androidx.lifecycle.MutableLiveData
import com.golde.cowries.Data.Rates
import com.golde.cowries.Data.Repo
import io.realm.RealmResults
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.annotations.Nullable
import kotlin.coroutines.CoroutineContext

class ConversionViewModel : BaseViewModel() {
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)
    private val repo : Repo = Repo()
    val rates = MutableLiveData<RealmResults<Rates>>()
    var to = MutableLiveData<String>("NGN")
    var from = MutableLiveData<String>("USD")
    var toRate = MutableLiveData<Double>(0.0)
    var toValue = MutableLiveData<Double>(0.0)
    var checkpoints = MutableLiveData<Map<String, Double>>()

    init {
        EventBus.getDefault().register(this)
    }


    @Subscribe
    fun handleEventBusData(data : HashMap<String, String>){
        this.to.postValue(data["to"])
        this.from.postValue(data["from"])
    }

    fun fetchRates(){
        if(repo.returnRates().isEmpty()) {
            scope.launch {
                repo.downloadRatesAndUpdateRealm()
            }
        }
    }

    fun listen4Rates() {
        rates.postValue(repo.rates)
        fetchRates()
        repo.returnRates().addChangeListener { it->
            rates.postValue(it)
        }
    }

//    fun historicalRates30(){
//        scope.launch {
//            val symbols = arrayOf(from.value.toString(), to.value.toString())
//            repo.History30Days(symbols)
//        }
//    }

    fun offerListOfCurrencies() : List<String>{
        val list : MutableList<String> = mutableListOf<String>()
        for(i in repo.returnRates()){
            list.add(i.key.toString())
        }
        return list
    }

    fun offerToRate() : Double {
        val eur2FromRate = repo.returnRates().where().equalTo("key", from.value.toString()).findFirst()?.rate
        val eur2ToRate = repo.returnRates().where().equalTo("key", to.value.toString()).findFirst()?.rate
        val newRate = (1 / eur2FromRate!!) / (1 / eur2ToRate!!)
        toRate.postValue(newRate)
        return newRate
    }

    fun convert(from : Double){
        toValue.postValue(from * offerToRate())
    }

    fun checkPointsStatus() : Boolean{
        return repo.cpSuccess
    }

    fun initCheckPoints() {
        scope.launch {
            checkpoints.postValue(repo.History30Days(arrayOf(from.value.toString(), to.value.toString())))
        }
    }

    fun CheckPointsDates() : Array<String> { return repo.dates }

    override fun onCleared() {
        EventBus.getDefault().unregister(this);
        super.onCleared()
    }
}