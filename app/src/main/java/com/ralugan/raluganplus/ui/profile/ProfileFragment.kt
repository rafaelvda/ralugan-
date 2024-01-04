package com.ralugan.raluganplus.ui.profile

import android.content.Intent
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
import com.google.firebase.database.FirebaseDatabase
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

        val emailEditText: EditText = binding.editTextEmail
        val passwordEditText: EditText = binding.editTextPassword
        val loginButton: Button = binding.loginButton
        val signupRedirectButton: Button = binding.signupRedirectButton

        val firstNameEditText: EditText = binding.editTextFirstName // Ajout du champ de prénom
        val emailSignUpEditText = binding.editTextEmailSignUp
        val passwordSignUpEditText = binding.editTextPasswordSignUp
        val signupButton: Button = binding.signupButton
        val backButton: Button = binding.backButton

        val logoutButton: Button = binding.logoutButton


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
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs afin de vous connectez", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val emailSignUp = emailSignUpEditText.text.toString()
            val passwordSignUp = passwordSignUpEditText.text.toString()

            if (emailSignUp.isNotEmpty() && passwordSignUp.isNotEmpty() && firstName.isNotEmpty()) {
                signUp(emailSignUp, passwordSignUp, firstName)
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs afin de vous inscrire", Toast.LENGTH_SHORT).show()
            }
        }

        signupRedirectButton.setOnClickListener {
            showRegistrationForm()
        }

        backButton.setOnClickListener {
            showLoginForm()
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

    private fun signUp(email: String, password: String, firstName : String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Enregistrement des données dans la base de données Realtime Firebase
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")

                        val userMap = HashMap<String, Any>()
                        userMap["uid"] = uid
                        userMap["email"] = email
                        userMap["firstName"] = firstName
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

    private fun signOut() {
        auth.signOut()
        showLoginForm()
    }

    private fun userIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun showLoggedInState() {
        // Cachez le formulaire de connexion, affichez le bouton de déconnexion, etc.
        binding.loginForm.visibility = View.INVISIBLE
        binding.signupButton.visibility = View.INVISIBLE
        binding.logoutButton.visibility = View.VISIBLE

        binding.signupRedirectButton.visibility = View.GONE
        // Autres opérations liées à l'état connecté
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE
        binding.signupRedirectButton.visibility = View.VISIBLE
        binding.logoutButton.visibility = View.GONE
        binding.logoutMessage.visibility = View.GONE

        binding.signupForm.visibility = View.GONE
        binding.editTextEmailSignUp.visibility = View.GONE
        binding.editTextPasswordSignUp.visibility = View.GONE
        binding.signupButton.visibility = View.GONE
        binding.backButton.visibility = View.GONE
    }


    private fun showRegistrationForm() {
        binding.loginForm.visibility = View.GONE
        binding.loginButton.visibility = View.GONE
        binding.signupRedirectButton.visibility = View.GONE
        binding.logoutButton.visibility = View.GONE
        binding.logoutMessage.visibility = View.GONE

        binding.signupForm.visibility = View.VISIBLE
        binding.editTextEmailSignUp.visibility = View.VISIBLE
        binding.editTextPasswordSignUp.visibility = View.VISIBLE
        binding.signupButton.visibility = View.VISIBLE
        binding.backButton.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



