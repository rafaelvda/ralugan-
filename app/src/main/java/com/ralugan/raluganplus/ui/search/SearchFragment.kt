package com.ralugan.raluganplus.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ralugan.raluganplus.ui.details.DetailsActivity
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.api.ApiClient
import com.ralugan.raluganplus.api.WikidataApi
import com.ralugan.raluganplus.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView: SearchView = view.findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Appel de la méthode de recherche ici
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Mettez à jour votre liste ou effectuez d'autres actions lorsqu'il y a un changement de texte
                return true
            }
        })

    }

    private fun performSearch(query: String?) {
        // Vous pouvez utiliser la nouvelle valeur de query pour construire votre requête SPARQL
        // et appeler l'API avec la nouvelle requête
        if (query != null) {
            // Remplacez votre requête SPARQL actuelle avec la nouvelle requête
            val sparqlQuery = """
                SELECT ?itemLabel ?pic
                WHERE {
                    {
                        ?seriesItem wdt:P1476 ?itemLabel. # Title
                        ?seriesItem wdt:P31 wd:Q5398426.  # Television series
                        ?seriesItem wdt:P750 wd:Q54958752.  # Platform = Disney+
            
                        FILTER(CONTAINS(UCASE(?itemLabel), UCASE('$query')))
                        OPTIONAL {
                            ?seriesItem wdt:P154 ?pic.
                        }
                    }
                    UNION
                    {
                        ?filmItem wdt:P1476 ?itemLabel. # Title
                        ?filmItem wdt:P31 wd:Q11424.  # Film
                        ?filmItem wdt:P750 wd:Q54958752.  # Platform = Disney+
            
                        FILTER(CONTAINS(UCASE(?itemLabel), UCASE('$query')))
                        OPTIONAL {
                            ?filmItem wdt:P154 ?pic.
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
            binding.textSearch.setOnClickListener {
                val clickedTitle = binding.textSearch.text.toString()

                // Create an Intent to start the new activity
                val intent = Intent(requireContext(), DetailsActivity::class.java)
                intent.putExtra("TITLE", clickedTitle)

                // Start the activity
                startActivity(intent)
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

                            val imageView = ImageView(requireContext())

                            // Créer un ImageView pour l'image
                            if (binding.has("pic")) {
                                val imageUrl = binding.getJSONObject("pic").getString("value").replace("http://", "https://")

                                // Utiliser Glide pour charger l'image dans l'ImageView
                                Glide.with(this)
                                    .load(imageUrl)
                                    .error(R.drawable.ralugan)
                                    .into(imageView)

                                // Ajouter le TextView et ImageView au LinearLayout
                                linearLayout.addView(titleTextView)
                                linearLayout.addView(imageView)
                            } else {
                                Glide.with(this)
                                    .load(R.drawable.ralugan)
                                    .into(imageView)
                                linearLayout.addView(titleTextView)
                                linearLayout.addView(imageView)
                            }

                            // Set an ID for the TextView to capture click event
                            titleTextView.id = View.generateViewId()

                            // Set click listener for the TextView
                            titleTextView.setOnClickListener {
                                val clickedTitle = titleTextView.text.toString()

                                // Create an Intent to start the new activity
                                val intent = Intent(requireContext(), DetailsActivity::class.java)
                                intent.putExtra("TITLE", clickedTitle)

                                // Start the activity
                                startActivity(intent)
                            }

                            imageView.setOnClickListener {
                                val clickedTitle = titleTextView.text.toString()

                                // Create an Intent to start the new activity
                                val intent = Intent(requireContext(), DetailsActivity::class.java)
                                intent.putExtra("TITLE", clickedTitle)

                                // Start the activity
                                startActivity(intent)
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
        textView.isClickable = true
        textView.isFocusable = true

        return textView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
