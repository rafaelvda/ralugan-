package com.ralugan.raluganplus

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Retrieve the title from the Intent
        val title = intent.getStringExtra("TITLE")

        // Now, you can use the title as needed in your DetailsActivity
        Log.d("DetailsActivity", "Received title: $title")

    }
}
