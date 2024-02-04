package com.ralugan.raluganplus.ui.details

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ralugan.raluganplus.R

class DetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DetailsAdapter
    private lateinit var viewModel: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Récupérer le titre depuis l'Intent
        val title = intent.getStringExtra("TITLE")

        // Maintenant, vous pouvez utiliser le titre comme nécessaire dans votre DetailsActivity
        Log.d("DetailsActivity", "Received title: $title")

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DetailsAdapter(emptyList())
        recyclerView.adapter = adapter

        val viewModelFactory = DetailsViewModelFactory(title ?: "")
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailsViewModel::class.java)

        viewModel.detailsItemList.observe(this, Observer {
            adapter.updateData(it)
        })

        viewModel.fetchDetails()
    }

}
