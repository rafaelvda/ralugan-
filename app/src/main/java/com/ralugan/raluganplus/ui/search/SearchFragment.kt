package com.ralugan.raluganplus.ui.search

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.target.Target
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
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
import retrofit2.Call
import retrofit2.Callback
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

        // Remplacez votre requête SPARQL actuelle avec la nouvelle requête
        val sparqlQuery = """
    SELECT ?itemLabel ?pic
    WHERE {
        {
            ?seriesItem wdt:P1476 ?itemLabel. # Title
            ?seriesItem wdt:P31 wd:Q5398426.  # Television series
            ?seriesItem wdt:P750 wd:Q54958752.  # Platform = Disney+

            FILTER(CONTAINS(UCASE(?itemLabel), UCASE('Star')))
            OPTIONAL {
                ?seriesItem wdt:P154 ?pic.
            }
        }
        UNION
        {
            ?filmItem wdt:P1476 ?itemLabel. # Title
            ?filmItem wdt:P31 wd:Q11424.  # Film
            ?filmItem wdt:P750 wd:Q54958752.  # Platform = Disney+

            FILTER(CONTAINS(UCASE(?itemLabel), UCASE('Star')))
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
    }

    private fun handleApiResponse(response: Response<ResponseBody>) {
        Log.d("SearchFragment", "Handling API response")
        val resultTextView = binding.textSearch
        val imageView = binding.imageView

        if (response.isSuccessful) {
            try {
                val jsonResult = JSONObject(response.body()?.string())

                // Vérifiez si la clé "results" existe dans la réponse JSON
                if (jsonResult.has("results")) {
                    val results = jsonResult.getJSONObject("results")

                    // Obtenez la liste des "bindings"
                    val bindings = results.getJSONArray("bindings")

                    // Vérifiez s'il y a au moins un résultat
                    if (bindings.length() > 0) {
                        // Utilisez StringBuilder pour concaténer tous les titres
                        val titlesBuilder = StringBuilder()

                        // Traitez chaque élément dans la liste des "bindings"
                        for (i in 0 until bindings.length()) {
                            val binding = bindings.getJSONObject(i)
                            val itemLabel = binding.getJSONObject("itemLabel").getString("value")
                            // Ajoutez le titre à la chaîne des titres
                            titlesBuilder.append("$itemLabel\n")

                            // Vérifiez si "pic" existe avant de charger l'image
                            if (binding.has("pic")) {
                                val imageUrl = binding.getJSONObject("pic").getString("value").replace("http://", "https://")
                                Log.d("SearchFragment", "Image URL: $imageUrl")  // Ajoutez cette ligne pour imprimer l'URL dans les logs

                                // Utilisez Glide pour charger l'image dans l'ImageView
                                Glide.with(this)
                                    .load(imageUrl)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            Log.e("SearchFragment", "Glide Load Failed", e)
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            Log.d("SearchFragment", "Image Loaded Successfully")
                                            return false
                                        }
                                    })
                                    .into(imageView)


                            } else {
                                // Si "pic" n'existe pas, vous pouvez gérer cela ici
                                Log.d("SearchFragment", "Aucune URL d'image trouvée")
                            }
                        }

                        // Affichez la liste des titres dans le TextView
                        resultTextView.text = titlesBuilder.toString()
                    } else {
                        // Aucun résultat trouvé
                        resultTextView.text = "Aucun résultat trouvé"
                    }
                } else {
                    // Gérer le cas où la clé "results" est absente dans la réponse JSON
                    resultTextView.text = "Aucun résultat trouvé"
                }
            } catch (e: JSONException) {
                Log.e("SearchFragment", "JSON parsing error: ${e.message}")
            }
        } else {
            Log.e("SearchFragment", "API call failed with code: ${response.code()}")
            // ... (rest of the error handling)
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
