package com.martintools.batteryinfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.LayerDrawable
import android.os.BatteryManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val batteryStatusIntentFilter  = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        this.registerReceiver(batteryBroadcastReceiver, batteryStatusIntentFilter )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private val batteryBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            updateProgressBar(batteryPercentage)

            val statusValue = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val statusField = findViewById<TextView>(R.id.status)
            val statusText = when (statusValue) {
                BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.status_charging)
                BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.status_discharging)
                BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.status_full)
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getString(R.string.status_not_charging)
                else -> getString(R.string.status_unknown)
            }
            statusField.text = statusText
            statusField.setTextColor(getColorByLevel(batteryPercentage))

            val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempValue = rawTemp / 10.0
            val tempField = findViewById<TextView>(R.id.temperature)
            tempField.text = getString(R.string.temperature_format, tempValue)
            tempField.setTextColor(getColorByTemp(tempValue.toInt()))

            val sourceField = findViewById<TextView>(R.id.source)
            sourceField.text = when(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)){
                BatteryManager.BATTERY_PLUGGED_AC -> getString(R.string.source_ac)
                BatteryManager.BATTERY_PLUGGED_USB -> getString(R.string.source_usb)
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> getString(R.string.source_wireless)
                BatteryManager.BATTERY_PLUGGED_DOCK -> getString(R.string.source_dock)
                else -> getString(R.string.source_not_plugged)
            }
            sourceField.setTextColor(getColorByLevel(batteryPercentage))

            val healthField = findViewById<TextView>(R.id.health)
            healthField.text = when(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)){
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> getString(R.string.health_overheat)
                BatteryManager.BATTERY_HEALTH_GOOD -> getString(R.string.health_good)
                BatteryManager.BATTERY_HEALTH_COLD -> getString(R.string.health_cold)
                BatteryManager.BATTERY_HEALTH_DEAD -> getString(R.string.health_dead)
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> getString(R.string.health_over_voltage)
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> getString(R.string.health_failed)
                else -> getString(R.string.health_unknown)
            }
            healthField.setTextColor(getColorByLevel(batteryPercentage))

            val technologyValue = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            val technologyField = findViewById<TextView>(R.id.technology)
            technologyField.text = technologyValue
            technologyField.setTextColor(getColorByLevel(batteryPercentage))

            val voltageValue = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()/1000
            val voltageField = findViewById<TextView>(R.id.voltage)
            voltageField.text = getString(R.string.voltage_format, voltageValue)
            voltageField.setTextColor(getColorByLevel(batteryPercentage))

            val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            val chargeTimeRemaining = batteryManager.computeChargeTimeRemaining()
            val chargeTimeField = findViewById<TextView>(R.id.charge_time)
            if (chargeTimeRemaining > 0) {
                val hours = chargeTimeRemaining / (1000 * 60 * 60)
                val minutes = (chargeTimeRemaining / (1000 * 60)) % 60
                chargeTimeField.text = if (hours > 0) {
                    getString(R.string.charge_time_hours_minutes, hours, minutes)
                } else {
                    getString(R.string.charge_time_minutes, minutes)
                }
            } else {
                chargeTimeField.text = if (statusValue == BatteryManager.BATTERY_STATUS_CHARGING) {
                    getString(R.string.calculating)
                } else {
                    getString(R.string.not_applicable)
                }
            }
            chargeTimeField.setTextColor(getColorByLevel(batteryPercentage))
        }
    }

    private fun getColorByLevel(percentage: Int): Int {
        val level0 = resources.getColor(R.color.level0, theme)
        val level1 = resources.getColor(R.color.level1, theme)
        val level2 = resources.getColor(R.color.level2, theme)
        val level3 = resources.getColor(R.color.level3, theme)
        val level4 = resources.getColor(R.color.level4, theme)
        val level5 = resources.getColor(R.color.level5, theme)

        val color = when {
            percentage >= 86 -> level5
            percentage >= 68 -> level4
            percentage >= 52 -> level3
            percentage >= 36 -> level2
            percentage >= 20 -> level1
            else -> level0
        }
        return color
    }

    private fun getColorByTemp(temperature: Int): Int {
        val level0 = resources.getColor(R.color.freezing, theme)
        val level1 = resources.getColor(R.color.cold, theme)
        val level2 = resources.getColor(R.color.cool, theme)
        val level3 = resources.getColor(R.color.perfect, theme)
        val level4 = resources.getColor(R.color.warm, theme)
        val level5 = resources.getColor(R.color.hot, theme)

        val color = when {
            temperature >= 48 -> level5
            temperature >= 35 -> level4
            temperature >= 20 -> level3
            temperature >= 10 -> level2
            temperature >= 0 -> level1
            else -> level0
        }
        return color
    }

    private fun updateProgressBar(progress: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressBarDrawable = progressBar.progressDrawable as LayerDrawable
        progressBar.progress = progress

        // Index of the second item in LayerDrawable that represents the color of the circle (circle.xml)
        val circleItemIndex = 1

        // Set new color based on level
        val newColor = getColorByLevel(progress)

        // Change the color of the circle
        val circleShape = progressBarDrawable.getDrawable(circleItemIndex)
        circleShape.setTint(newColor)
        progressBar.progressDrawable = progressBarDrawable

        // Update text inside of circle
        findViewById<TextView>(R.id.progress).text = getString(R.string.battery_percentage, progress)
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
                val shareSub = getString(R.string.share_subject)
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(myIntent, getString(R.string.share_via)))
                true
            }
            R.id.info_btn -> {
                Toast.makeText(applicationContext, getString(R.string.created_by), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.github_btn -> {
                val gitHubUrl = resources.getString(R.string.app_github)
                val intent = Intent(Intent.ACTION_VIEW, gitHubUrl.toUri())
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