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
import androidx.navigation.NavController
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
import com.ralugan.raluganplus.databinding.FragmentLoginBinding
import java.lang.Exception

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    lateinit var imageView: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = binding.editTextEmail
        val passwordEditText: EditText = binding.editTextPassword
        val loginButton: Button = binding.loginButton
        val signupRedirectButton: Button = binding.signupRedirectButton

        // Vérifiez si l'utilisateur est connecté
        if (userIsLoggedIn()) {
            findNavController().navigate(R.id.navigation_profile)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Veuillez remplir tous les champs afin de vous connectez",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        signupRedirectButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_signup)
        }

    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Connexion réussie
                        findNavController().navigate(R.id.navigation_profile)
                } else {
                    // La connexion a échoué
                    Toast.makeText(
                        requireContext(),
                        "Erreur de connexion: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun userIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



