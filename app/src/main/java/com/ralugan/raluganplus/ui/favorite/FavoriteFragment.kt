package com.ralugan.raluganplus.ui.favorite


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ralugan.raluganplus.R
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ralugan.raluganplus.dataclass.RaluganPlus

class FavoriteFragment : Fragment() {

    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var deleteFavoriteButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deleteButton = view.findViewById<ImageButton>(R.id.deleteFavoriteButton)

        favoriteAdapter = FavoriteAdapter(emptyList())

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = favoriteAdapter

        deleteButton?.setOnClickListener {
            showUnderConstructionDialog()
        }

        loadCurrentUserFavorites()
    }

    private fun loadCurrentUserFavorites() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Vérifier si l'utilisateur est connecté
        if (currentUser != null) {
            val uid = currentUser.uid

            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users").child(uid).child("listFavorite")

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoritesList = mutableListOf<RaluganPlus>()

                    for (childSnapshot in snapshot.children) {
                        val favorite = childSnapshot.getValue(RaluganPlus::class.java)
                        favorite?.let { favoritesList.add(it) }
                    }


                    favoriteAdapter.updateData(favoritesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gérer l'erreur
                }
            })
        }
    }

    private fun showUnderConstructionDialog() {
        // Créez une boîte de dialogue (AlertDialog) pour afficher le message "En construction"
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("En cours de construction")
        builder.setMessage("Nous travaillons sur cette fonctionnalité. Revenez bientôt pour les dernières mises à jour !")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Affichez la boîte de dialogue
        val dialog = builder.create()
        dialog.show()
    }
}