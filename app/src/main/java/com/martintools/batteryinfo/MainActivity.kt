package com.martintools.batteryinfo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val batteryStatusIntentFilter  = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        this.registerReceiver(batteryBroadcastReceiver, batteryStatusIntentFilter )
    }

    private val batteryBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            updateProgressBar(batteryPercentage)

            /*val statusValue = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
            val statusImage = findViewById<ImageView>(R.id.status_power)
            if (statusValue == 2) statusImage.visibility = View.VISIBLE else statusImage.visibility = View.INVISIBLE*/

            val statusValue = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
            findViewById<ImageView>(R.id.status_power).visibility = if (statusValue == 2) View.VISIBLE else View.INVISIBLE


            val tempValue = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10
            val tempField = findViewById<TextView>(R.id.temperature)
            tempField.text = "$tempValue \u00B0C\n"

            val sourceField = findViewById<TextView>(R.id.source)
            when(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)){
                BatteryManager.BATTERY_PLUGGED_AC -> sourceField.text = "AC charger"
                BatteryManager.BATTERY_PLUGGED_USB -> sourceField.text = "USB connection"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> sourceField.text = "Wireless charging"
                else -> sourceField.text = "Not Plugged"
            }

            val healthField = findViewById<TextView>(R.id.health)
            when(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)){
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> healthField.text = "Overheated"
                BatteryManager.BATTERY_HEALTH_GOOD -> healthField.text = "Good"
                BatteryManager.BATTERY_HEALTH_COLD -> healthField.text = "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> healthField.text = "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> healthField.text = "Over voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> healthField.text = "Failed"
                else -> healthField.text = "Unknown"
            }

            val technologyValue = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            findViewById<TextView>(R.id.technology).text = technologyValue

            val voltageValue = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()/1000
            findViewById<TextView>(R.id.voltage).text = "$voltageValue V"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressBar(progress: Int) {
        findViewById<ProgressBar>(R.id.progress_bar).progress = progress
        findViewById<TextView>(R.id.text_view_progress).text = "$progress%"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_btn -> {
                val myIntent = Intent(Intent.ACTION_SEND)
                myIntent.type = "text/plain"
                val shareBody = resources.getString(R.string.app_playstore)
                val shareSub = "With this app you can view battery information. Try it out!"
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(myIntent, "Share via"))
                true
            }
            R.id.info_btn -> {
                Toast.makeText(applicationContext,"Created by Martin",Toast.LENGTH_SHORT).show()
                true
            }
            R.id.github_btn -> {
                val gitHubUrl = resources.getString(R.string.app_github)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gitHubUrl))
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(batteryBroadcastReceiver)
        super.onDestroy()
    }
}