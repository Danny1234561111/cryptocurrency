package com.example.getusdservicetemplate

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val usdRate = MutableLiveData<String>()
    val rateCheckInteractor = RateCheckInteractor()

    fun onCreate() {
        refreshRate()
    }

    fun onRefreshClicked() {
        refreshRate()
    }

    private fun refreshRate() {
        GlobalScope.launch(Dispatchers.Main) {
            val rate = rateCheckInteractor.requestRate()
            Log.d(TAG, "usdRate = $rate")
            usdRate.value = rate
        }
    }

    companion object {
        var fsym=""
        var tsyms = mutableListOf<String>()
        const val TAG = "MainViewModel"
        const val api_key="09ccfa59e79557e3d79ca07a4e626478183b849cf9b628cfb1d7d1d6ba50c257"
        fun getUsdRateUrl(): String {
            return "https://min-api.cryptocompare.com/data/price?fsym=$fsym&tsyms=${tsyms.joinToString(",")}&apikey=$api_key"
        }
    }
}

