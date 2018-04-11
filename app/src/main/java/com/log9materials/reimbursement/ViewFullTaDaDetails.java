package com.log9materials.reimbursement;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class ViewFullTaDaDetails extends AppCompatActivity {

    ImageView transactionImage;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_full_ta_da_details);

        url = getIntent().getStringExtra("url");

        transactionImage = (ImageView) findViewById(R.id.Image);

        Log.v("url", url);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_image)
                .into(transactionImage);
    }

}
