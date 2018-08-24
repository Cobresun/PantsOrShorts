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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobresun.brun.pantsorshorts.presenter.MainActivityPresenter;
import com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository;
import com.cobresun.brun.pantsorshorts.view.MainActivityView;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;
    private ImageView img;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        updateView();
    }

    @Override
    public void updateView() {
        presenter = new MainActivityPresenter(this, new SharedPrefsUserDataRepository(getApplicationContext()), getApplicationContext());
        presenter.loadUserThreshold();
        presenter.checkInternet();
        presenter.getLocation(this);
        presenter.updateClothing();
    }

    @Override
    public void displayUserThreshold(float userThreshold) {
        // do nothing right now
    }

    @Override
    public void displayCity(String city) {
        if (city != null && !city.equals("failed")) {
            TextView cityNameText = findViewById(R.id.city_name);
            cityNameText.setText(city);
            cityNameText.invalidate();
        }
        else {
            presenter.getLocation(this);
        }
    }

    @Override
    public void displayTemperature(float temperature) {
        TextView tempText = findViewById(R.id.temperature);
        tempText.setText(temperature + "\u00B0" + "C");
        tempText.invalidate();
    }

    @Override
    public void displayYouShouldWearText(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            textView.setText("You should wear pants today");
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            textView.setText("You should wear shorts today");
        }
        textView.invalidate();
    }

    @Override
    public void displayClothingImage(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            img.setTag("pants");
            img.setImageResource(R.drawable.pants);
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            img.setTag("shorts");
            img.setImageResource(R.drawable.shorts);
        }
        img.invalidate();
    }

    @Override
    public void displayButton(int clothing) {
        if (clothing == MainActivityPresenter.PANTS) {
            button.setText("It's too hot for pants");
            button.setBackgroundResource(R.drawable.my_button_red);
            Drawable sun = getApplicationContext().getResources().getDrawable(R.drawable.ic_wb_sunny);
            button.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null);
        }
        else if (clothing == MainActivityPresenter.SHORTS) {
            button.setText("It's too cold for shorts");
            Drawable snow = getApplicationContext().getResources().getDrawable(R.drawable.ic_ac_unit);
            button.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null);
            button.setBackgroundResource(R.drawable.my_button_blue);
        }
        button.invalidate();
    }

    @Override
    public void displayNoInternet() {
        Toast.makeText(getApplicationContext(), "Internet unavialable, please connect.", Toast.LENGTH_SHORT).show();
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
}