package com.ralugan.raluganplus.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ralugan.raluganplus.R
import com.ralugan.raluganplus.api.ApiClient
import com.ralugan.raluganplus.api.WikidataApi
import com.ralugan.raluganplus.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ralugan.raluganplus.ui.details.DetailsActivity
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import androidx.viewpager2.widget.ViewPager2


class HomeFragment<YourResponseType : Any?> : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    private lateinit var horizontalLinearLayout: LinearLayout
    private lateinit var horizontalLinearLayout2: LinearLayout
    private lateinit var horizontalLinearLayout3: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        horizontalLinearLayout = rootView.findViewById(R.id.horizontalLinearLayout)
        horizontalLinearLayout2 = rootView.findViewById(R.id.horizontalLinearLayout2)
        horizontalLinearLayout3 = rootView.findViewById(R.id.horizontalLinearLayout3)


        nouveauSurRaluganPlus()
        Drama()
        Crime()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val disneyButton = view.findViewById<ImageButton>(R.id.customButtonDisney)
        val marvelButton = view.findViewById<ImageButton>(R.id.customButtonMarvel)
        val starWarsButton = view.findViewById<ImageButton>(R.id.customButton4)
        val pixarButton = view.findViewById<ImageButton>(R.id.customButton2)
        val natGeoButton = view.findViewById<ImageButton>(R.id.customButton7)
        val starplusButton = view.findViewById<ImageButton>(R.id.customButtonStar)

        disneyButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_disney)
        }
        marvelButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_marvel)
        }
        starWarsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_starwars)
        }
        pixarButton.setOnClickListener {
            showUnderConstructionDialog()
        }
        natGeoButton.setOnClickListener {
            showUnderConstructionDialog()
        }
        starplusButton.setOnClickListener {
            showUnderConstructionDialog()
        }
        nouveauSurRaluganPlus()
    }

    private fun nouveauSurRaluganPlus() {
        val sparqlQuery = """
        SELECT ?itemLabel ?pic ?date
        WHERE {
            ?item wdt:P1476 ?itemLabel. # Title
            ?item wdt:P580 ?date.
            #?item wdt:P31 wd:Q11424.  # Film
            ?item wdt:P31 wd:Q5398426.  # Television series
            ?item wdt:P750 wd:Q54958752.  # Platform = Disney+
            OPTIONAL{
                ?item wdt:P154 ?pic.
            }
        }
        ORDER BY DESC(BOUND(?pic)) DESC(?date)
        LIMIT 14
    """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = wikidataApi.getDisneyPlusInfo(sparqlQuery, "json").execute()

                withContext(Dispatchers.Main) {
                    handleApiResponse(response,  horizontalLinearLayout)
                }
            } catch (e: Exception) {
                // Gérer l'exception
            }
        }
    }

    private fun Drama() {
        val sparqlQuery = """
        SELECT ?itemLabel ?pic
        WHERE {
            ?item wdt:P1476 ?itemLabel. # Title
            ?item wdt:P136 wd:Q1366112. # GenreDrama
            #?item wdt:P31 wd:Q11424.  # Film
            ?item wdt:P31 wd:Q5398426.  # Television series
            ?item wdt:P750 wd:Q54958752.  # Platform = Disney+
            OPTIONAL{
                ?item wdt:P154 ?pic.
            }
        }
        ORDER BY DESC (?pic)
        LIMIT 15
    """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = wikidataApi.getDisneyPlusInfo(sparqlQuery, "json").execute()

                withContext(Dispatchers.Main) {
                    handleApiResponse(response,  horizontalLinearLayout2)
                }
            } catch (e: Exception) {
                // Gérer l'exception
            }
        }
    }

    private fun Crime() {
        val sparqlQuery = """
        SELECT ?itemLabel ?pic
        WHERE {
            ?item wdt:P1476 ?itemLabel. # Title
            ?item wdt:P136 wd:Q9335577. # GenreDrama
            #?item wdt:P31 wd:Q11424.  # Film
            ?item wdt:P31 wd:Q5398426.  # Television series
            ?item wdt:P750 wd:Q54958752.  # Platform = Disney+
            OPTIONAL{
                ?item wdt:P154 ?pic.
            }
        }
        ORDER BY DESC (?pic)
        LIMIT 30
    """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = wikidataApi.getDisneyPlusInfo(sparqlQuery, "json").execute()

                withContext(Dispatchers.Main) {
                    handleApiResponse(response,  horizontalLinearLayout3)
                }
            } catch (e: Exception) {
                // Gérer l'exception
            }
        }
    }


    private fun handleApiResponse(response: Response<ResponseBody>, horizontalLinearLayout: LinearLayout) {
        // Effacer les résultats précédents
        horizontalLinearLayout.removeAllViews()

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
                                Glide.with(requireContext())
                                    .load(imageUrl)
                                    .override(500, 500) // Remplacez 300 par la taille souhaitée en pixels
                                    .into(imageView)

                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                params.rightMargin = 10 // Ajustez cette valeur en fonction de l'espace souhaité

                                // Ajouter le TextView et ImageView à la disposition horizontale avec les marges
                                horizontalLinearLayout.addView(imageView, params)
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
                        horizontalLinearLayout.addView(createTextView("Aucun résultat trouvé"))
                    }
                } else {
                    horizontalLinearLayout.addView(createTextView("Aucun résultat trouvé"))
                }
            } catch (e: JSONException) {
                horizontalLinearLayout.addView(createTextView("Erreur de traitement JSON"))
                Log.e("SearchFragment", "JSON parsing error: ${e.message}")
            }
        } else {
            horizontalLinearLayout.addView(createTextView("Erreur de chargement des données"))
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

    private fun showUnderConstructionDialog() {
        // Créez une boîte de dialogue (AlertDialog) pour afficher le message "En construction"
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("En cours de construction")
        builder.setMessage("Nous travaillons sur cette fonctionnalité. Revenez bientôt pour les dernières mises à jour !")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Affichez la boîte de dialogue
        val dialog = builder.create()
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}