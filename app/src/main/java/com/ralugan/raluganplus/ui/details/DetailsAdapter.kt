package com.ralugan.raluganplus.ui.details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.dataclass.DetailsItem

class DetailsAdapter(private var detailsItemList: List<DetailsItem>) :
    RecyclerView.Adapter<DetailsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
        val awardTextView: TextView = itemView.findViewById(R.id.awardTextView)
        val costTextView: TextView = itemView.findViewById(R.id.costTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val dirTextView: TextView = itemView.findViewById(R.id.dirTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val episodesTextView: TextView = itemView.findViewById(R.id.episodesTextView)
        val seasonsTextView: TextView = itemView.findViewById(R.id.seasonsTextView)
        val genreTextView: TextView = itemView.findViewById(R.id.genreTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemdetails, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detailsItem = detailsItemList[position]

        holder.titleTextView.text = detailsItem.itemLabel
        holder.noteTextView.text = detailsItem.note
        holder.awardTextView.text = detailsItem.award
        holder.costTextView.text = detailsItem.cost
        holder.dateTextView.text = detailsItem.date
        holder.dirTextView.text = detailsItem.dir
        holder.durationTextView.text = detailsItem.duration
        holder.episodesTextView.text = detailsItem.episodes
        holder.seasonsTextView.text = detailsItem.seasons
        holder.genreTextView.text = detailsItem.genre
        detailsItem.pic?.let { Log.e("DetailsAdapter", it) }
        // Chargez l'image avec Glide (si l'URL de l'image n'est pas null)
        if (!detailsItem.pic.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(detailsItem.pic)
                .error(R.drawable.ralugan)
                .into(holder.imageView)
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.ralugan)
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
        return detailsItemList.size
    }

    fun updateData(newList: List<DetailsItem>) {
        detailsItemList = newList
        notifyDataSetChanged()
    }
}


