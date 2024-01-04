package com.ralugan.raluganplus.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.databinding.FragmentEditprofileBinding

import com.ralugan.raluganplus.databinding.FragmentLoginBinding
import com.ralugan.raluganplus.databinding.FragmentSignupBinding

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
        val passwordEditText = binding.editTextPassword
        val confirmPasswordEditText = binding.editTextConfirmPassword

        val confirmButton: Button = binding.confirmButton
        val backButton: Button = binding.backButton

        // Initialiser l'ImageView
        imageView = view.findViewById(R.id.imageView)

        // Trouver le bouton par son ID
        val selectImageButton: Button = view.findViewById(R.id.selectImageButton)

        // Ajouter un gestionnaire de clic au bouton
        selectImageButton.setOnClickListener {
            // Code pour sélectionner une image à partir du répertoire du téléphone
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        confirmButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = passwordEditText.text.toString()

            if(imageUri != null && firstName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                editProfile(password, firstName, imageUri)
            }
            else if (firstName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                editProfile(password, firstName, imageUri)
            }
            else if (imageUri != null && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                editProfile(password, firstName, imageUri)
            }
            else if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                editProfile(password, firstName, imageUri)
            }
            else if (imageUri != null && firstName.isNotEmpty()) {
                editProfile(password, firstName, imageUri)
            }
            else if (firstName.isNotEmpty()){
                editProfile(password, firstName, imageUri)
            }
            else if (imageUri != null){
                editProfile(password, firstName, imageUri)
            }
            else {
                Toast.makeText(
                    requireContext(),
                    "Veuillez remplir tous les champs afin de vous inscrire",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

    }

    private fun editProfile(password: String, firstName: String, imageUri: Uri?) {

    }

    private fun signUp(email: String, password: String, firstName: String, imageUri: Uri?) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Enregistrement des données dans la base de données Realtime Firebase
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(uid, imageUri)
                        }
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")

                        val userMap = HashMap<String, Any>()
                        userMap["uid"] = uid
                        userMap["email"] = email
                        userMap["firstName"] = firstName

                        // Ajoutez l'URL de l'image à la carte si disponible
                        if (imageUri != null) {
                            val imageUrl =
                                "https://firebasestorage.googleapis.com/v0/b/raluganplus.appspot.com/o/userProfile%2F${uid}.jpeg?alt=media"
                            userMap["imageUrl"] = imageUrl
                        } else {
                            userMap["imageUrl"] = ""
                        }

                        usersRef.child(uid).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Succès de l'enregistrement dans la base de données
                                    Toast.makeText(
                                        requireContext(),
                                        "Inscription réussie",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Erreur d'enregistrement dans la base de données",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // L'inscription a échoué
                        Toast.makeText(
                            requireContext(),
                            "Erreur d'inscription: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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
