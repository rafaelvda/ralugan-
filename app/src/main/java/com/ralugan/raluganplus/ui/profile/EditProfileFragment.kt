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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseError
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.databinding.FragmentEditprofileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditprofileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditprofileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val firstNameEditText: EditText = binding.editTextFirstName
        val currentPasswordEditText = binding.editTextCurrentPassword
        val newPasswordEditText = binding.editTextNewPassword

        val confirmButton: Button = binding.confirmButton
        val backButton: Button = binding.backButton

        // Initialiser l'ImageView
        imageView = view.findViewById(R.id.imageView)

        // Trouver le bouton par son ID
        val selectImageButton: Button = view.findViewById(R.id.selectImageButton)

        // Récupérer l'UID de l'utilisateur actuellement connecté
        val currentUserId = auth.currentUser?.uid

        // Référence à la base de données Firebase Realtime
        val databaseReference = FirebaseDatabase.getInstance().reference

        // Vérifier si l'UID est non nul
        currentUserId?.let { userId ->
            // Référence spécifique à l'utilisateur dans la base de données
            val userReference = databaseReference.child("users").child(userId)

            // Écouter les changements dans la base de données pour cet utilisateur
            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Récupérer les données de l'utilisateur
                        val firstName = snapshot.child("firstName").value.toString()
                        val imageUrl = snapshot.child("imageUrl").value.toString()

                        // Mettre à jour les champs d'édition et l'URL de l'image
                        firstNameEditText.setText(firstName)
                        imageUri = Uri.parse(imageUrl)

                        // Charger et afficher l'image avec votre méthode
                        loadImage(imageUrl)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gérer les erreurs de lecture depuis la base de données
                }
            })
        }

        // Ajouter un gestionnaire de clic au bouton
        selectImageButton.setOnClickListener {
            // Code pour sélectionner une image à partir du répertoire du téléphone
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        confirmButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            editProfile(currentPassword, newPassword, firstName, imageUri)
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

    }

    private fun editProfile(currentPassword: String, newPassword: String, firstName: String, imageUri: Uri?) {
        val user = auth.currentUser

        var profileUpdated = false

        // Vérifier si l'utilisateur est connecté
        if (user != null) {
            // Vérifier si un changement d'image est demandé
            if (imageUri != null) {
                // Mettre à jour l'image de profil
                updateProfileImage(user.uid, imageUri)
                profileUpdated = true
            }

            // Vérifier si un changement de prénom est demandé
            if (firstName != user.displayName) {
                // Mettre à jour le prénom de l'utilisateur
                updateFirstName(user.uid, firstName)
                profileUpdated = true
            }

            // Vérifier si un changement de mot de passe est demandé
            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                // Prompt the user to reauthenticate
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // User has been reauthenticated, proceed with password update
                            updatePassword(user, newPassword)
                            profileUpdated = true
                        } else {
                            profileUpdated = false
                            // Reauthentication failed, handle the error
                            Toast.makeText(
                                requireContext(),
                                "Le mot de passe actuel est incorrect",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                // Si aucune mise à jour de mot de passe n'est nécessaire, afficher le message
                showSuccessMessage(profileUpdated)
            }
        }
    }

    private fun showSuccessMessage(success: Boolean) {
        // Afficher le message uniquement si toutes les mises à jour ont été effectuées avec succès
        if (success) {
            Toast.makeText(
                requireContext(),
                "Profil mis à jour avec succès",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateFirstName(userId: String, newFirstName: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersReference: DatabaseReference = database.getReference("users")

        // Mettre à jour le prénom de l'utilisateur dans la base de données
        usersReference.child(userId).child("firstName").setValue(newFirstName)
            .addOnSuccessListener {
                // Succès de la mise à jour
                Log.d("ProfileUpdate", "Prénom mis à jour avec succès")
            }
            .addOnFailureListener {
                // Échec de la mise à jour
                Log.d("ProfileUpdate", "Échec de la mise à jour du prénom")
            }
    }

    private fun updatePassword(user: FirebaseUser, newPassword: String) {
        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Succès de la mise à jour du mot de passe
                    showSuccessMessage(true)
                } else {
                    // Échec de la mise à jour du mot de passe
                    Toast.makeText(
                        requireContext(),
                        "Échec de la mise à jour du mot de passe: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showSuccessMessage(false)
                }
            }
    }


    private fun updateProfileImage(userId: String, imageUri: Uri) {
        // Référence à l'emplacement actuel de l'image de profil
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageReference: StorageReference = storage.getReference("userProfile")
            .child("image_${System.currentTimeMillis()}.jpeg")

        // Récupérer l'ancien lien de l'image de profil
        databaseReference.child("imageUrl").get().addOnSuccessListener { dataSnapshot ->
            val oldImageUrl: String? = dataSnapshot.value as? String

            // Si une ancienne image existe, la supprimer
            if (!oldImageUrl.isNullOrEmpty()) {
                val oldImageReference: StorageReference = storage.getReferenceFromUrl(oldImageUrl)
                oldImageReference.delete().addOnSuccessListener {
                    // Succès de la suppression de l'ancienne image
                    Log.d("ProfileUpdate", "Ancienne image supprimée avec succès.")
                }.addOnFailureListener {
                    // Échec de la suppression de l'ancienne image
                    Log.e("ProfileUpdate", "Échec de la suppression de l'ancienne image.", it)
                }
            }

            // Télécharger la nouvelle image sur Firebase Storage
            storageReference.putFile(imageUri)
                .addOnSuccessListener {
                    // Succès du téléchargement
                    // Récupérer le lien de téléchargement de la nouvelle image téléchargée
                    storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Mettre à jour le lien de la nouvelle image de profil dans la base de données
                        updateProfileImageUrl(userId, downloadUri.toString())
                    }.addOnFailureListener {
                        // Échec de la récupération du lien de téléchargement
                        Log.d("ProfileUpdate", "Échec de la récupération du lien de téléchargement")
                    }
                }
                .addOnFailureListener {
                    // Échec du téléchargement
                    Log.d("ProfileUpdate", "Échec du téléchargement de la nouvelle image de profil")
                }
        }.addOnFailureListener {
            // Échec de la récupération de l'ancien lien de l'image de profil
            Log.d("ProfileUpdate", "Échec de la récupération de l'ancien lien de l'image de profil")
        }
    }

    private fun updateProfileImageUrl(userId: String, imageUrl: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersReference: DatabaseReference = database.getReference("users")

        // Mettre à jour le lien de l'image de profil dans la base de données
        usersReference.child(userId).child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                // Succès de la mise à jour
                Log.d("ProfileUpdate", "Image de profil mise à jour avec succès")
            }
            .addOnFailureListener {
                // Échec de la mise à jour
                Log.d("ProfileUpdate", "Échec de la récupération de l'ancien lien de l'image de profil")
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadImage(imageUrl: String) {
        // Utiliser une bibliothèque comme Picasso ou Glide pour charger et afficher l'image
        // Exemple avec Picasso :
        Glide.with(requireContext())
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform()) // Option pour afficher une image circulaire, facultatif
            .into(imageView)
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
