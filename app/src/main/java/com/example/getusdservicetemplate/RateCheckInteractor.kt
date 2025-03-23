package com.example.getusdservicetemplate

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
class RateCheckInteractor {
    val networkClient = NetworkClient()

    suspend fun requestRate(): String {
        return withContext(Dispatchers.IO) {
            MainViewModel.fsym = selectedOption1
            MainViewModel.tsyms.add(selectedOption)
            val result = networkClient.request(MainViewModel.getUsdRateUrl())
            if (!result.isNullOrEmpty()) {
                parseRate(result)
            } else {
                ""
            }
        }
    }
    suspend fun subscribeRate(): String {
        return withContext(Dispatchers.IO) {
            MainViewModel.fsym = selectedOption3
            MainViewModel.tsyms.add(selectedOption2)
            val result = networkClient.request(MainViewModel.getUsdRateUrl())
            if (!result.isNullOrEmpty()) {
                parseRate(result)
            } else {
                ""
            }
        }
    }

    private fun parseRate(jsonString: String): String {
        try {
            Log.d("RateCheckInteractor", "Response: $jsonString")
            return JSONObject(jsonString).getString(selectedOption)
        } catch (e: Exception) {

            Log.e("RateCheckInteractor", "", e)
        }
        return ""
    }
}