package com.example.notetakingapp4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView fullImageView = findViewById(R.id.full_image);
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra("image_uri"));
        fullImageView.setImageURI(imageUri);
    }
}