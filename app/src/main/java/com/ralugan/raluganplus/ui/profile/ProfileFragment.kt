package com.ralugan.raluganplus.ui.profile

import android.app.Activity
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

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    lateinit var imageView: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val logoutButton: Button = binding.logoutButton


        // Vérifiez si l'utilisateur est connecté
        if (userIsLoggedIn()) {
            val logoImageView: ImageView = binding.logoImageView
            val userNameTextView: TextView = binding.userNameTextView

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


        logoutButton.setOnClickListener {
            signOut()
        }
    }


    // Fonction pour télécharger une image sur Firebase Storage
    private fun uploadImageToFirebaseStorage(uid: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().getReference("userProfile")
        val imageRef = storageRef.child("image_${System.currentTimeMillis()}.jpeg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // L'image a été téléchargée avec succès
                // Récupérez l'URL de téléchargement
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Mettez à jour la base de données avec l'URL de l'image
                    updateProfileImageInDatabase(uid, uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Une erreur s'est produite lors du téléchargement de l'image
                Toast.makeText(
                    requireContext(),
                    "Erreur de téléchargement de l'image: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfileImageInDatabase(uid: String, imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // Mettez à jour l'URL de l'image dans la base de données
        usersRef.child(uid).child("imageUrl").setValue(imageUrl)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // Succès de la mise à jour de l'image dans la base de données
                    Toast.makeText(
                        requireContext(),
                        "Image de profil mise à jour avec succès",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erreur de mise à jour de l'image de profil dans la base de données",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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


    // Assurez-vous de définir le code de demande IMAGE_PICK_CODE
    companion object {
        const val IMAGE_PICK_CODE = 1000
    }

    // Gérez le résultat de la sélection d'image dans onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // L'utilisateur a sélectionné une image
            imageUri = data.data
            // Mettez à jour l'ImageView avec l'image sélectionnée
            imageView.setImageURI(imageUri)
        }
    }


}



