package com.ralugan.raluganplus.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ralugan.raluganplus.databinding.FragmentProfileBinding
import androidx.navigation.fragment.findNavController
import com.ralugan.raluganplus.R

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

        val loginButton: Button = binding.loginButton
        val signupButton: Button = binding.signupButton
        val logoutButton: Button = binding.logoutButton
        val emailEditText: EditText = binding.editTextEmail
        val passwordEditText: EditText = binding.editTextPassword

        // Vérifiez si l'utilisateur est connecté
        if (userIsLoggedIn()) {
            showLoggedInState()
        } else {
            showLoginForm()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            // Utilisez l'action spécifiée dans le fichier de navigation pour naviguer vers le SignupFragment
            findNavController().navigate(R.id.action_profileFragment_to_signupFragment)
        }

        logoutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Connexion réussie
                    showLoggedInState()
                } else {
                    // La connexion a échoué
                    Toast.makeText(requireContext(), "Erreur de connexion: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        showLoginForm()
    }

    private fun userIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun showLoggedInState() {
        // Cachez le formulaire de connexion, affichez le bouton de déconnexion, etc.
        binding.loginForm.visibility = View.GONE
        binding.signupButton.visibility = View.GONE
        binding.logoutButton.visibility = View.VISIBLE
        // Autres opérations liées à l'état connecté
    }

    private fun showLoginForm() {
        // Affichez le formulaire de connexion, cachez le bouton de déconnexion, etc.
        binding.loginForm.visibility = View.VISIBLE
        binding.signupButton.visibility = View.VISIBLE
        binding.logoutButton.visibility = View.GONE
        // Autres opérations liées à l'état déconnecté
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



