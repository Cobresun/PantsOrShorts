/**
 * PANTS OR SHORTS
 *
 * App that informs the user whether or not they should wear pants because it is cold,
 * or shorts if it is hot, depending on the weather of the user's city, based on user preference.
 *
 * Produced by Brian Norman and Sunny Nagam
 * Cobresun - August 2018
 */

package com.cobresun.brun.pantsorshorts

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cobresun.brun.pantsorshorts.Clothing.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class MainActivity : AppCompatActivity(), MainActivityView {

    private val presenter: MainActivityPresenter by lazy {
        MainActivityPresenter(this, SharedPrefsUserDataRepository(applicationContext), applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Objects.requireNonNull<ActionBar>(supportActionBar).hide()
        setContentView(R.layout.activity_main)

        nightModeSwitch.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) {
                presenter.toggleNightMode()
            }
        }

        mainButton.setOnClickListener {
            presenter.calibrateThreshold()
            Toast.makeText(applicationContext, getString(R.string.remember_that), Toast.LENGTH_SHORT).show()
        }

        updateView()
    }

    override fun updateView() {
        presenter.checkInternet()
        presenter.createLocationRequest(this)
        presenter.setupNightMode()
    }

    override fun displayCity(city: String?) {
        when (city) {
            null -> presenter.createLocationRequest(this)
            else -> {
                city_name.text = city
                city_name.invalidate()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun displayTemperature(temperature: Int, isCelsius: Boolean) {
        when {
            isCelsius -> temperatureTextView.text = "$temperature\u00B0C"
            else -> {
                val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
                temperatureTextView.text = "$fahrenheit\u00B0F"
            }
        }
        temperatureTextView.invalidate()
    }

    @SuppressLint("SetTextI18n")
    override fun displayHighTemperature(temperature: Int, isCelsius: Boolean) {
        when {
            isCelsius -> temperatureHighTextView.text = temperature.toString() + "\u00B0"
            else -> {
                val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
                temperatureHighTextView.text = fahrenheit.toString() + "\u00B0"
            }
        }
        temperatureHighTextView.invalidate()
    }

    @SuppressLint("SetTextI18n")
    override fun displayLowTemperature(temperature: Int, isCelsius: Boolean) {
        when {
            isCelsius -> temperatureLowTextView.text = temperature.toString() + "\u00B0"
            else -> {
                val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
                temperatureLowTextView.text = fahrenheit.toString() + "\u00B0"
            }
        }
        temperatureLowTextView.invalidate()
    }

    override fun displayYouShouldWearText(clothing: Clothing) {
        when (clothing) {
            PANTS -> shouldWearTextView.text = getString(R.string.feels_like_pants)
            SHORTS -> shouldWearTextView.text = getString(R.string.feels_like_shorts)
            UNKNOWN -> TODO()
        }
        shouldWearTextView.invalidate()
    }

    override fun displayClothingImage(clothing: Clothing) {
        when (clothing) {
            PANTS -> {
                clothingImageView.tag = getString(R.string.pants)
                clothingImageView.setImageResource(R.drawable.pants)
            }
            SHORTS ->{
                clothingImageView.tag = getString(R.string.shorts)
                clothingImageView.setImageResource(R.drawable.shorts)
            }
            UNKNOWN -> TODO()
        }
        clothingImageView.invalidate()
    }

    override fun displayButton(clothing: Clothing) {
        when (clothing) {
            PANTS -> {
                mainButton.text = getString(R.string.too_hot)
                mainButton.setBackgroundResource(R.drawable.my_button_red)
                val sun = applicationContext.resources.getDrawable(R.drawable.ic_wb_sunny, null)
                mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)
            }
            SHORTS -> {
                mainButton.text = getString(R.string.too_cold)
                val snow = applicationContext.resources.getDrawable(R.drawable.ic_ac_unit, null)
                mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
                mainButton.setBackgroundResource(R.drawable.my_button_blue)
            }
            UNKNOWN -> TODO()
        }
        mainButton.invalidate()
    }

    override fun displayNoInternet() {
        Toast.makeText(applicationContext, getString(R.string.internet_unavailable), Toast.LENGTH_SHORT).show()
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, MainActivityPresenter.INITIAL_PERMS, MainActivityPresenter.INITIAL_REQUEST)
    }

    override fun displayNoPermissionsEnabled() {
        Toast.makeText(applicationContext, getString(R.string.enable_permission), Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MainActivityPresenter.INITIAL_REQUEST) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                updateView()
            } else {
                displayNoPermissionsEnabled()
                requestPermissions()
            }
        }
    }

    override fun changeTempMode(view: View) {
        presenter.updateTempMode()
    }

    // TODO: Replace with material theming
    override fun displayNightMode(isNightMode: Boolean) {
        val darkColor = Color.parseColor("#212121")
        val lightColor = Color.parseColor("#FAFAFA")
        when {
            isNightMode -> {
                rootLayout.setBackgroundColor(darkColor)
                city_name.setTextColor(lightColor)
                shouldWearTextView.setTextColor(lightColor)
                nightModeImage.setColorFilter(lightColor)
            }
            else -> {
                rootLayout.setBackgroundColor(lightColor)
                city_name.setTextColor(darkColor)
                shouldWearTextView.setTextColor(darkColor)
                nightModeImage.setColorFilter(darkColor)
            }
        }
        nightModeSwitch.isChecked = isNightMode
    }
}
