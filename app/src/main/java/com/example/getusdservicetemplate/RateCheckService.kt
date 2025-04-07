package com.example.getusdservicetemplate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal

class RateCheckService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var rateCheckAttempt = 0
    private lateinit var startRate: BigDecimal
    private lateinit var previousRate: BigDecimal
    private val rateCheckInteractor = RateCheckInteractor()
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val CHANNEL_ID = "CHANNEL_ID"

    private val rateCheckRunnable: Runnable = Runnable {
        requestAndCheckRate()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Rate Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Notifications for currency rate changes"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestAndCheckRate() {
        if (rateCheckAttempt >= RATE_CHECK_ATTEMPTS_MAX) {
            Log.d(TAG, "Maximum attempts reached. Stopping service.")
            stopSelf()
            return
        }

        coroutineScope.launch {
            val currentRateString = rateCheckInteractor.subscribeRate()
            Log.d(TAG, "Current rate string: $currentRateString")

            if (currentRateString.isNotEmpty()) {
                try {
                    val currentRate = BigDecimal(currentRateString)
                    Log.d(TAG, "Current rate: $currentRate")

                    // Если курс изменился, отправляем уведомление
                    if (!::previousRate.isInitialized || currentRate != previousRate) {
                        val direction = if (currentRate > previousRate) "up" else "down"
                        showNotification(currentRate, direction)
                        previousRate = currentRate // Обновляем предыдущее значение
                    }

                    rateCheckAttempt = 0 // Сбрасываем попытки, если курс успешно получен
                    handler.postDelayed(rateCheckRunnable, RATE_CHECK_INTERVAL)

                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Failed to parse current rate: $currentRateString", e)
                    handler.post {
                        Toast.makeText(this@RateCheckService, "Failed to get rate!", Toast.LENGTH_SHORT).show()
                    }
                    stopSelf()
                }
            } else {
                Log.w(TAG, "Received empty rate. Stopping service.")
                handler.post {
                    Toast.makeText(this@RateCheckService, "Failed to get rate!", Toast.LENGTH_SHORT).show()
                }
                stopSelf()
            }
        }
    }

    private fun showNotification(currentRate: BigDecimal,up: String) {
        if (up=="up") {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cat)
                .setContentTitle("${selectedOption3} Rate Update UP")
                .setContentText("Current rate: $currentRate")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            with(NotificationManagerCompat.from(this)) {
                notify(7, builder.build())
            }
        }
        if (up=="down") {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cat)
                .setContentTitle("${selectedOption3} Rate Update DOWN")
                .setContentText("Current rate: $currentRate")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            with(NotificationManagerCompat.from(this)) {
                notify(7, builder.build())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startRateString = intent?.getStringExtra(ARG_START_RATE)

        if (!startRateString.isNullOrEmpty()) {
            try {
                startRate = BigDecimal(startRateString)
                previousRate = startRate
                Log.d(TAG, "onStartCommand startRate = $startRate")
                rateCheckAttempt = 0
                handler.post(rateCheckRunnable)
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Failed to parse start or target rate", e)
                Toast.makeText(this, "Invalid start or target rate!", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        } else {
            Log.e(TAG, "Invalid start or target rate")
            Toast.makeText(this, "Invalid start or target rate!", Toast.LENGTH_SHORT).show()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rateCheckRunnable)
        job.cancel() // Отменяем корутины при уничтожении сервиса
    }

    companion object {
        const val TAG = "RateCheckService"
        const val RATE_CHECK_INTERVAL = 5000L
        const val RATE_CHECK_ATTEMPTS_MAX = 100

        const val ARG_START_RATE = "ARG_START_RATE"

        fun startService(context: Context, startRate: String) {
            context.startService(Intent(context, RateCheckService::class.java).apply {
                putExtra(ARG_START_RATE, startRate)
            })
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, RateCheckService::class.java))
        }
    }
}

