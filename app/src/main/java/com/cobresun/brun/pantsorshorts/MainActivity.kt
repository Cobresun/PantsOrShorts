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

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), MainActivityView {

    private val presenter: MainActivityPresenter by lazy { MainActivityPresenter(this, SharedPrefsUserDataRepository(applicationContext), applicationContext) }

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
            Toast.makeText(applicationContext, "Pants or Shorts will remember that.", Toast.LENGTH_SHORT).show()
        }

        updateView()
    }

    override fun updateView() {
        // ***** Hacky fix to solve current crash, can probably remove in the future  ***** \\
        presenter.clearThresholdIfUserUpdatedOrFirstTimeLaunch()

        presenter.checkInternet()
        presenter.createLocationRequest(this)
        presenter.setupNightMode()
    }

    override fun displayCity(city: String) {
        if (city != "failed") {
            city_name.text = city
            city_name.invalidate()
        } else {
            presenter.createLocationRequest(this)
        }
    }

    override fun displayTemperature(tempInt: Int, isCelsius: Boolean) {
        if (isCelsius) {
            temperature.text = "$tempInt\u00B0C"
        } else {
            val fahrenheit = (tempInt * (9.0 / 5.0).toFloat()).toInt() + 32
            temperature.text = "$fahrenheit\u00B0F"
        }
        temperature.invalidate()
    }

    override fun displayHighTemperature(tempInt: Int, isCelsius: Boolean) {
        if (isCelsius) {
            temperatureHigh.text = tempInt.toString() + "\u00B0"
        } else {
            val fahrenheit = (tempInt * (9.0 / 5.0).toFloat()).toInt() + 32
            temperatureHigh.text = fahrenheit.toString() + "\u00B0"
        }
        temperatureHigh.invalidate()
    }

    override fun displayLowTemperature(tempInt: Int, isCelsius: Boolean) {
        if (isCelsius) {
            temperatureLow.text = tempInt.toString() + "\u00B0"
        } else {
            val fahrenheit = (tempInt * (9.0 / 5.0).toFloat()).toInt() + 32
            temperatureLow.text = fahrenheit.toString() + "\u00B0"
        }
        temperatureLow.invalidate()
    }

    override fun displayYouShouldWearText(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            shouldWearTextView.text = getString(R.string.feels_like_pants)
        } else if (clothing == MainActivityPresenter.SHORTS) {
            shouldWearTextView.text = getString(R.string.feels_like_shorts)
        }
        shouldWearTextView.invalidate()
    }

    override fun displayClothingImage(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            clothingImageView.tag = "pants"
            clothingImageView.setImageResource(R.drawable.pants)
        } else if (clothing == MainActivityPresenter.SHORTS) {
            clothingImageView.tag = "shorts"
            clothingImageView.setImageResource(R.drawable.shorts)
        }
        clothingImageView.invalidate()
    }

    override fun displayButton(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            mainButton.text = getString(R.string.too_hot)
            mainButton.setBackgroundResource(R.drawable.my_button_red)
            val sun = applicationContext.resources.getDrawable(R.drawable.ic_wb_sunny)
            mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)
        } else if (clothing == MainActivityPresenter.SHORTS) {
            mainButton.text = getString(R.string.too_cold)
            val snow = applicationContext.resources.getDrawable(R.drawable.ic_ac_unit)
            mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
            mainButton.setBackgroundResource(R.drawable.my_button_blue)
        }
        mainButton.invalidate()
    }

    override fun displayNoInternet() {
        Toast.makeText(applicationContext, "Internet unavailable, please connect.", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, MainActivityPresenter.INITIAL_PERMS, MainActivityPresenter.INITIAL_REQUEST)
    }

    override fun displayNoPermissionsEnabled() {
        Toast.makeText(applicationContext, "This app won't even work if you don't enable permission...", Toast.LENGTH_LONG).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    override fun displayNightMode(isNightMode: Boolean) {
        val darkColor = Color.parseColor("#212121")
        val lightColor = Color.parseColor("#FAFAFA")
        if (isNightMode) {
            rootLayout.setBackgroundColor(darkColor)
            city_name.setTextColor(lightColor)
            shouldWearTextView.setTextColor(lightColor)
            nightModeImage.setColorFilter(lightColor)
        } else {
            rootLayout.setBackgroundColor(lightColor)
            city_name.setTextColor(darkColor)
            shouldWearTextView.setTextColor(darkColor)
            nightModeImage.setColorFilter(darkColor)
        }
        nightModeSwitch.isChecked = isNightMode
    }

}