package com.ralugan.raluganplus.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.api.ApiClient
import com.ralugan.raluganplus.api.WikidataApi
import com.ralugan.raluganplus.databinding.FragmentStarwarsBinding
import com.ralugan.raluganplus.dataclass.RaluganPlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import kotlin.math.log

class StarWarsFragment : Fragment() {

    private var _binding: FragmentStarwarsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarwarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)

        val sparqlQuery = """
            SELECT ?itemLabel ?pic
            WHERE {
            {
                ?filmItem wdt:P1476 ?itemLabel. # Title
                ?filmItem wdt:P31 wd:Q11424.  # Film
                ?filmItem wdt:P750 wd:Q54958752.  # Platform = Disney+
                ?filmItem wdt:P272 wd:Q242446. # LucasFilm

                OPTIONAL {
                ?filmItem wdt:P154 ?pic.
                }
            }
            UNION
            {
                ?seriesItem wdt:P1476 ?itemLabel. # Title
                ?seriesItem wdt:P31 wd:Q5398426.  # Television series
                ?seriesItem wdt:P750 wd:Q54958752.  # Platform = Disney+
                ?seriesItem wdt:P272 wd:Q242446. # LucasFilm

                OPTIONAL {
                ?seriesItem wdt:P154 ?pic.
                }
            }
            }
            ORDER BY DESC (?pic)
        """.trimIndent()

        // Appel de l'API dans une coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = wikidataApi.getDisneyPlusInfo(sparqlQuery, "json").execute()

                activity?.runOnUiThread {
                    handleApiResponse(response)
                }
            } catch (e: Exception) {
                // Gérer l'exception
            }
        }
    }

    private fun handleApiResponse(response: Response<ResponseBody>) {
        val linearLayout = binding.linearLayout

        // Effacer les résultats précédents
        linearLayout.removeAllViews()

        if (response.isSuccessful) {
            try {
                val jsonResult = JSONObject(response.body()?.string())

                if (jsonResult.has("results")) {
                    val results = jsonResult.getJSONObject("results")
                    val bindings = results.getJSONArray("bindings")

                    if (bindings.length() > 0) {
                        for (i in 0 until bindings.length()) {
                            val binding = bindings.getJSONObject(i)
                            val itemLabel = binding.getJSONObject("itemLabel").getString("value")

                            // Créer un TextView pour le titre
                            val titleTextView = TextView(requireContext())
                            titleTextView.text = itemLabel

                            // Créer un ImageView pour l'image
                            if (binding.has("pic")) {
                                val imageUrl = binding.getJSONObject("pic").getString("value").replace("http://", "https://")
                                val imageView = ImageView(requireContext())

                                // Utiliser Glide pour charger l'image dans l'ImageView
                                Glide.with(this)
                                    .load(imageUrl)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageView)

                                // Ajouter le TextView et ImageView au LinearLayout
                                linearLayout.addView(titleTextView)
                                linearLayout.addView(imageView)

                                // Ajouter un cœur cliquable
                                val heartButton = ImageButton(requireContext())
                                heartButton.setImageResource(R.drawable.ic_coeur)

                                val sizeInPixels = resources.getDimensionPixelSize(R.dimen.heart_size)
                                heartButton.layoutParams = ViewGroup.LayoutParams(sizeInPixels, sizeInPixels)

                                heartButton.setOnClickListener {
                                    // Ajouter le film à la liste des favoris de l'utilisateur
                                    addMovieToFavorites(auth.currentUser?.uid, itemLabel, imageUrl)
                                }
                                linearLayout.addView(heartButton)
                            } else {
                                // Si "pic" n'existe pas, ajouter seulement le TextView
                                linearLayout.addView(titleTextView)
                            }
                        }
                    } else {
                        linearLayout.addView(createTextView("Aucun résultat trouvé"))
                    }
                } else {
                    linearLayout.addView(createTextView("Aucun résultat trouvé"))
                }
            } catch (e: JSONException) {
                linearLayout.addView(createTextView("Erreur de traitement JSON"))
                Log.e("SearchFragment", "JSON parsing error: ${e.message}")
            }
        } else {
            linearLayout.addView(createTextView("Erreur de chargement des données"))
            Log.e("SearchFragment", "API call failed with code: ${response.code()}")
            // ... (rest of the error handling)
        }
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        return textView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addMovieToFavorites(uid: String?, movieTitle: String, movieImageUrl: String) {
        if (uid != null) {
            // Vérifier si le film est déjà dans la liste des favoris
            isMovieInFavorites(uid, movieTitle) { isAlreadyInFavorites ->
                if (isAlreadyInFavorites) {
                    Log.d("star wars", "$isAlreadyInFavorites")
                    // Afficher un message indiquant que le film est déjà dans les favoris
                    Toast.makeText(
                        requireContext(),
                        "Le film est déjà dans la liste des favoris",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Ajouter le film à la liste des favoris
                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users").child(uid).child("listFavorite")

                    val newFavorite = RaluganPlus(movieTitle, movieImageUrl)
                    usersRef.push().setValue(newFavorite)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                // Succès de l'ajout du film aux favoris
                                Toast.makeText(
                                    requireContext(),
                                    "Film ajouté aux favoris avec succès",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Erreur lors de l'ajout du film aux favoris",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }
    private fun isMovieInFavorites(uid: String?, movieTitle: String, onComplete: (Boolean) -> Unit) {
        if (uid != null) {
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users").child(uid).child("listFavorite")

            usersRef.orderByChild("title").equalTo(movieTitle)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        onComplete(dataSnapshot.exists())
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Gérer l'erreur
                        onComplete(false)
                    }
                })
        } else {
            onComplete(false)
        }
    }

}