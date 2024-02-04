package com.ralugan.raluganplus.ui.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.dataclass.RaluganPlus

class FavoriteAdapter(private var favoriteMovies: List<RaluganPlus>) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = favoriteMovies[position]

        // Mettez à jour les vues de l'élément avec les données du film
        holder.titleTextView.text = movie.title
        Glide.with(holder.itemView.context)
            .load(movie.imageUrl)
            .override(3000,4000)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return favoriteMovies.size
    }

    fun updateData(newData: List<RaluganPlus>) {
        favoriteMovies = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}