package com.ralugan.raluganplus.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ralugan.raluganplus.databinding.FragmentProfileBinding
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ralugan.raluganplus.R
import java.lang.Exception

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val editProfileButton: Button = view.findViewById(R.id.editProfileButton)
        val watchListButton: Button = view.findViewById(R.id.watchListButton)
        val settingsButton: Button = view.findViewById(R.id.settingsButton)
        val accountButton: Button = view.findViewById(R.id.accountButton)
        val legalButton: Button = view.findViewById(R.id.legalButton)
        val helpButton: Button = view.findViewById(R.id.helpButton)
        val logoutButton: Button = binding.logoutButton


        // Vérifiez si l'utilisateur est connecté
        if (userIsLoggedIn()) {
            val logoImageView: ImageView = binding.logoImageView
            val userNameTextView: TextView = binding.userNameTextView

            editProfileButton.setOnClickListener {
                findNavController().navigate(R.id.navigation_editprofile)
            }

            watchListButton.setOnClickListener {
                showUnderConstructionDialog()
            }

            settingsButton.setOnClickListener {
                showUnderConstructionDialog()
            }

            accountButton.setOnClickListener {
                showUnderConstructionDialog()
            }

            legalButton.setOnClickListener {
                showUnderConstructionDialog()
            }

            helpButton.setOnClickListener {
                showUnderConstructionDialog()
            }

            logoutButton.setOnClickListener {
                showLogoutConfirmationDialog()
            }

            // Récupérez le prénom de l'utilisateur depuis la base de données
            val user = auth.currentUser
            val uid = user?.uid

            if (uid != null) {
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("users")

                usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val firstName = snapshot.child("firstName").value.toString()
                            val imageUrl = snapshot.child("imageUrl").value.toString()

                            if (!imageUrl.isNullOrEmpty()) {
                                // Utilisez Glide pour charger l'image dans votre ImageView
                                Glide.with(requireContext())
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(logoImageView)
                            } else {
                                // Gérer le cas où l'URL de l'image est vide ou nulle
                                Log.e("DEBUG", "Image URL is empty or null")
                                Glide.with(requireContext())
                                    .load(R.drawable.default_image)
                                    .circleCrop()
                                    .into(logoImageView)
                            }
                            // Affichez le prénom de l'utilisateur
                            userNameTextView.text = "$firstName"


                        }
                        else {
                            print("NOT EXISTS")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Gérez les erreurs ici
                        Log.e("ERROR", "DatabaseError: ${error.message}")
                    }
                })
            }
            else {
                print("UID NULL")
            }

    }
        else {
            findNavController().navigate(R.id.navigation_login)
        }
    }

    private fun signOut() {
        auth.signOut()
        findNavController().navigate(R.id.navigation_login)
    }

    private fun userIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun showLogoutConfirmationDialog() {
        // Créez une boîte de dialogue (AlertDialog) pour confirmer la déconnexion
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Déconnexion")
        builder.setMessage("Voulez-vous vraiment vous déconnecter ?")
        builder.setPositiveButton("Déconnexion") { _, _ ->
            signOut()
        }
        builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }

        // Affichez la boîte de dialogue
        val dialog = builder.create()
        dialog.show()
    }


}



