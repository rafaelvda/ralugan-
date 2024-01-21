package com.ralugan.raluganplus.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.api.ApiClient
import com.ralugan.raluganplus.api.WikidataApi
import com.ralugan.raluganplus.databinding.FragmentDisneyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

class DisneyFragment : Fragment() {

    private var _binding: FragmentDisneyBinding? = null
    private val binding get() = _binding!!

    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sparqlQuery = """
            SELECT ?itemLabel ?pic
            WHERE {
            {
                ?filmItem wdt:P1476 ?itemLabel. # Title
                ?filmItem wdt:P31 wd:Q11424.  # Film
                ?filmItem wdt:P750 wd:Q54958752.  # Platform = Disney+
                ?filmItem wdt:P272 wd:Q191224. # Disney

                OPTIONAL {
                ?filmItem wdt:P154 ?pic.
                }
            }
            UNION
            {
                ?seriesItem wdt:P1476 ?itemLabel. # Title
                ?seriesItem wdt:P31 wd:Q5398426.  # Television series
                ?seriesItem wdt:P750 wd:Q54958752.  # Platform = Disney+
                ?seriesItem wdt:P272 wd:Q191224. # Disney

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
}