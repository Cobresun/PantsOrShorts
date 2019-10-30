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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity(), MainActivityView {

    private var presenter: MainActivityPresenter? = null
    private var rootLayout: ConstraintLayout? = null
    private var clothingImageView: ImageView? = null
    private var mainButton: Button? = null
    private var shouldWearTextView: TextView? = null
    private var cityNameView: TextView? = null
    private var nightModeImage: ImageView? = null
    private var nightModeSwitch: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Objects.requireNonNull<ActionBar>(supportActionBar).hide()
        setContentView(R.layout.activity_main)

        clothingImageView = findViewById(R.id.clothingImageView)
        mainButton = findViewById(R.id.mainButton)
        shouldWearTextView = findViewById(R.id.shouldWearTextView)
        rootLayout = findViewById(R.id.rootLayout)
        cityNameView = findViewById(R.id.city_name)
        nightModeImage = findViewById(R.id.nightModeImage)
        nightModeSwitch = findViewById(R.id.nightModeSwitch)

        nightModeSwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            // On startup setChecked() is done programmatically in displayNightMode() so this avoids a double toggle
            if (!buttonView.isPressed) {
                return@OnCheckedChangeListener
            }

            presenter!!.toggleNightMode()
        })

        updateView()
    }

    override fun updateView() {
        presenter = MainActivityPresenter(this, SharedPrefsUserDataRepository(applicationContext), applicationContext)
        // ***** Hacky fix to solve current crash, can probably remove in the future  ***** \\
        presenter!!.clearThresholdIfUserUpdatedOrFirstTimeLaunch()
        presenter!!.checkInternet()
        presenter!!.createLocationRequest(this)
        presenter!!.setupNightMode()
    }

    override fun displayCity(city: String) {
        if (city != null && city != "failed") {
            cityNameView!!.text = city
            cityNameView!!.invalidate()
        } else {
            presenter!!.createLocationRequest(this)
        }
    }

    override fun displayTemperature(temperature: Int, isCelsius: Boolean) {
        val tempText = findViewById<TextView>(R.id.temperature)
        if (isCelsius) {
            tempText.text = "$temperature\u00B0C"
        } else {
            val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
            tempText.text = "$fahrenheit\u00B0F"
        }
        tempText.invalidate()
    }

    override fun displayHighTemperature(temperature: Int, isCelsius: Boolean) {
        val tempText = findViewById<TextView>(R.id.temperatureHigh)
        if (isCelsius) {
            tempText.text = temperature.toString() + "\u00B0"
        } else {
            val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
            tempText.text = fahrenheit.toString() + "\u00B0"
        }
        tempText.invalidate()
    }

    override fun displayLowTemperature(temperature: Int, isCelsius: Boolean) {
        val tempText = findViewById<TextView>(R.id.temperatureLow)
        if (isCelsius) {
            tempText.text = temperature.toString() + "\u00B0"
        } else {
            val fahrenheit = (temperature * (9.0 / 5.0).toFloat()).toInt() + 32
            tempText.text = fahrenheit.toString() + "\u00B0"
        }
        tempText.invalidate()
    }

    override fun displayYouShouldWearText(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            shouldWearTextView!!.text = "For the next few hours, it feels like pants weather"
        } else if (clothing == MainActivityPresenter.SHORTS) {
            shouldWearTextView!!.text = "For the next few hours, it feels like shorts weather"
        }
        shouldWearTextView!!.invalidate()
    }

    override fun displayClothingImage(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            clothingImageView!!.tag = "pants"
            clothingImageView!!.setImageResource(R.drawable.pants)
        } else if (clothing == MainActivityPresenter.SHORTS) {
            clothingImageView!!.tag = "shorts"
            clothingImageView!!.setImageResource(R.drawable.shorts)
        }
        clothingImageView!!.invalidate()
    }

    override fun displayButton(clothing: Int) {
        if (clothing == MainActivityPresenter.PANTS) {
            mainButton!!.text = "It's too hot for pants"
            mainButton!!.setBackgroundResource(R.drawable.my_button_red)
            val sun = applicationContext.resources.getDrawable(R.drawable.ic_wb_sunny)
            mainButton!!.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)
        } else if (clothing == MainActivityPresenter.SHORTS) {
            mainButton!!.text = "It's too cold for shorts"
            val snow = applicationContext.resources.getDrawable(R.drawable.ic_ac_unit)
            mainButton!!.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
            mainButton!!.setBackgroundResource(R.drawable.my_button_blue)
        }
        mainButton!!.invalidate()
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

    fun onButtonClick(view: View) {
        presenter!!.calibrateThreshold()
        Toast.makeText(applicationContext, "Pants or Shorts will remember that.", Toast.LENGTH_SHORT).show()
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
        presenter!!.updateTempMode()
    }

    override fun displayNightMode(isNightMode: Boolean) {
        val darkColor = Color.parseColor("#212121")
        val lightColor = Color.parseColor("#FAFAFA")
        if (isNightMode) {
            rootLayout!!.setBackgroundColor(darkColor)
            cityNameView!!.setTextColor(lightColor)
            shouldWearTextView!!.setTextColor(lightColor)
            nightModeImage!!.setColorFilter(lightColor)
        } else {
            rootLayout!!.setBackgroundColor(lightColor)
            cityNameView!!.setTextColor(darkColor)
            shouldWearTextView!!.setTextColor(darkColor)
            nightModeImage!!.setColorFilter(darkColor)
        }
        nightModeSwitch!!.isChecked = isNightMode
    }

}