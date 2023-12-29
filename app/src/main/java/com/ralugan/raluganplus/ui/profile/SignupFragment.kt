package com.ralugan.raluganplus.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ralugan.raluganplus.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = binding.editTextEmail
        passwordEditText = binding.editTextPassword

        val signupButton: Button = binding.signupButton
        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUp(email, password)
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton: Button = binding.backButton
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Inscription réussie
                    Toast.makeText(requireContext(), "Inscription réussie", Toast.LENGTH_SHORT).show()
                    // Rediriger vers la page de profil ou faire d'autres actions nécessaires
                } else {
                    // L'inscription a échoué
                    Toast.makeText(requireContext(), "Erreur d'inscription: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
