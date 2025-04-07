package com.example.getusdservicetemplate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.ArrayAdapter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
private val valuete = arrayOf("USD","RUB","EUR")
private val cryptovaluete = arrayOf("BTC","ETH","LTC")
var selectedOption: String = valuete[0]
var selectedOption1: String = cryptovaluete[0]
var selectedOption2: String = valuete[0]
var selectedOption3: String = cryptovaluete[0]
class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    lateinit var textRate: TextView
    lateinit var textTargetRate: EditText
    lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val CHANNEL_ID = "CHANNEL_ID"
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_DEFAULT)
        } else {
            TODO("VERSION.SDK_INT < O")
        }


        val currencyListView: ListView = findViewById(R.id.listViewCurrency)
        val cryptoListView: ListView = findViewById(R.id.listViewCrypto)
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,valuete)
        val adapter2 = ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,cryptovaluete)
        currencyListView.adapter =adapter
        cryptoListView.adapter = adapter2
        currencyListView.choiceMode=ListView.CHOICE_MODE_SINGLE
        cryptoListView.choiceMode=ListView.CHOICE_MODE_SINGLE

        currencyListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedOption = valuete[position]
        }
        cryptoListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedOption1 = cryptovaluete[position]
        }
        currencyListView.setItemChecked(0, true)
        cryptoListView.setItemChecked(0, true)

        initViewModel()
        initView()
    }


    override fun onDestroy() {
        super.onDestroy()

    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.usdRate.observe(this, {
            textRate.text = "$it $selectedOption"
        })



        viewModel.onCreate()
    }

    fun initView() {
        textRate = findViewById(R.id.textUsdRubRate)
        rootView = findViewById(R.id.rootView)

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            viewModel.onRefreshClicked()
        }

        findViewById<Button>(R.id.btnSubscribeToRate).setOnClickListener {
            selectedOption2= selectedOption
            selectedOption3= selectedOption1
            val startRate = viewModel.usdRate.value

            if (startRate?.isNotEmpty() == true) {
                RateCheckService.stopService(this)
                RateCheckService.startService(this, startRate)
            } else if (startRate.isNullOrEmpty()) {
                Snackbar.make(rootView, R.string.current_rate_empty, Snackbar.LENGTH_SHORT).show()
            }
        }

    }
}