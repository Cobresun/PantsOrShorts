package com.cobresun.brun.pantsorshorts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v){
        System.out.println("Main Button Pressed");
        ImageView img=(ImageView)findViewById(R.id.imageView);

        if(img.getTag() == "shorts"){
            img.setTag("pants");
            img.setImageResource(R.drawable.pants);
        }
        else{
            img.setTag("shorts");
            img.setImageResource(R.drawable.shorts);
        }
        /*
        if(img.getVisibility() == View.VISIBLE)
            img.setVisibility(View.INVISIBLE);
        else
            img.setVisibility(View.VISIBLE);
        */
    }
}
