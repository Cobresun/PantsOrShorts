/**
 * PANTS OR SHORTS
 *
 * App that informs the user whether or not they should wear pants because it is cold,
 * or shorts if it is hot, depending on the weather of the user's city, based on user preference.
 *
 * Produced by Brian Norman and Sunny Nagam
 * Cobresun - August 2018
 */

package com.cobresun.brun.pantsorshorts;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cobresun.brun.pantsorshorts.presenter.MainActivityPresenter;
import com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository;
import com.cobresun.brun.pantsorshorts.view.MainActivityView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;
    private ConstraintLayout rootLayout;
    private ImageView clothingImageView;
    private Button mainButton;
    private TextView shouldWearTextView;
    private TextView cityNameView;
    private ImageView nightModeImage;
    private Switch nightModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        clothingImageView = findViewById(R.id.clothingImageView);
        mainButton = findViewById(R.id.mainButton);
        shouldWearTextView = findViewById(R.id.shouldWearTextView);
        rootLayout = findViewById(R.id.rootLayout);
        cityNameView = findViewById(R.id.city_name);
        nightModeImage = findViewById(R.id.nightModeImage);
        nightModeSwitch = findViewById(R.id.nightModeSwitch);

        updateView();
    }

    @Override
    public void updateView() {
        presenter = new MainActivityPresenter(this, new SharedPrefsUserDataRepository(getApplicationContext()), getApplicationContext());
        presenter.checkInternet();
        presenter.getLocation(this);
        presenter.setupNightMode();
    }

    @Override
    public void displayCity(String city) {
        if (city != null && !city.equals("failed")) {
            cityNameView.setText(city);
            cityNameView.invalidate();
        }
        else {
            presenter.getLocation(this);
        }
    }

    @Override
    public void displayTemperature(float temperature, boolean isCelsius) {
        TextView tempText = findViewById(R.id.temperature);
        if (isCelsius) {
            tempText.setText(temperature + "\u00B0" + "C");
        }
        else {
            float fahrenheit = (int)(temperature * (float)(9.0/5.0))+ 32f;
            tempText.setText(fahrenheit + "\u00B0" + "F");
        }
        tempText.invalidate();
    }

    @Override
    public void displayYouShouldWearText(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            shouldWearTextView.setText("You should wear pants today");
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            shouldWearTextView.setText("You should wear shorts today");
        }
        shouldWearTextView.invalidate();
    }

    @Override
    public void displayClothingImage(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            clothingImageView.setTag("pants");
            clothingImageView.setImageResource(R.drawable.pants);
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            clothingImageView.setTag("shorts");
            clothingImageView.setImageResource(R.drawable.shorts);
        }
        clothingImageView.invalidate();
    }

    @Override
    public void displayButton(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            mainButton.setText("It's too hot for pants");
            mainButton.setBackgroundResource(R.drawable.my_button_red);
            Drawable sun = getApplicationContext().getResources().getDrawable(R.drawable.ic_wb_sunny);
            mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null);
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            mainButton.setText("It's too cold for shorts");
            Drawable snow = getApplicationContext().getResources().getDrawable(R.drawable.ic_ac_unit);
            mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null);
            mainButton.setBackgroundResource(R.drawable.my_button_blue);
        }
        mainButton.invalidate();
    }

    @Override
    public void displayNoInternet() {
        Toast.makeText(getApplicationContext(), "Internet unavailable, please connect.", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this, MainActivityPresenter.INITIAL_PERMS, MainActivityPresenter.INITIAL_REQUEST);
    }

    @Override
    public void displayNoPermissionsEnabled() {
        Toast.makeText(getApplicationContext(), "This app won't even work if you don't enable permission...", Toast.LENGTH_LONG).show();
    }

    public void onButtonClick(View view) {
        presenter.calibrateThreshold();
        Toast.makeText(getApplicationContext(), "Pants or Shorts will remember that.", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MainActivityPresenter.INITIAL_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                updateView();
            }
            else {
                displayNoPermissionsEnabled();
                requestPermissions();
            }
        }
    }

    public void changeTempMode(View view) {
        presenter.updateTempMode();
    }

    @Override
    public void displayNightMode(boolean isNightMode) {
        int darkColor = Color.parseColor("#212121");
        int lightColor = Color.parseColor("#FAFAFA");
        if (isNightMode) {
            nightModeSwitch.setChecked(true);
            rootLayout.setBackgroundColor(darkColor);
            cityNameView.setTextColor(lightColor);
            shouldWearTextView.setTextColor(lightColor);
            nightModeImage.setColorFilter(lightColor);
        }
        else {
            nightModeSwitch.setChecked(false);
            rootLayout.setBackgroundColor(lightColor);
            cityNameView.setTextColor(darkColor);
            shouldWearTextView.setTextColor(darkColor);
            nightModeImage.setColorFilter(darkColor);
        }
    }

    public void onNightModeSwitchd(View view) {
        presenter.toggleNightMode();
    }
}