package com.ralugan.raluganplus.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        // Obtenez la référence de l'ImageButton
        //val customButton = findViewById<ImageButton>(R.id.customButtonMarvel)

        // Ajoutez un écouteur de clic au bouton si nécessaire
        //customButton.setOnClickListener {
            // Logique à exécuter lors du clic sur le bouton
        //}

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textViewNews
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
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
        //movies()
    }

    private fun movies() {
        // Vous pouvez utiliser la nouvelle valeur de query pour construire votre requête SPARQL
        // et appeler l'API avec la nouvelle requête
            // Remplacez votre requête SPARQL actuelle avec la nouvelle requête
            val sparqlQuery = """
            SELECT ?itemLabel ?pic
            WHERE {
            ?item wdt:P1476 ?itemLabel. # Title
            ?item wdt:P31 wd:Q11424.  # Film
            ?item wdt:P750 wd:Q54958752.  # Platform = Disney+
                OPTIONAL{
                ?item wdt:P154 ?pic.
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
        val horizontalScrollView1 = view?.findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)

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

                            // Créer un CardView pour chaque résultat
                            val cardView = CardView(requireContext())
                            cardView.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            cardView.radius = context?.let { 8f.dpToPx(it).toFloat() }!!
                            cardView.cardElevation = context?.let { 8f.dpToPx(it).toFloat() }!!

                            // Créer un LinearLayout à l'intérieur du CardView
                            val linearLayout = LinearLayout(requireContext())
                            linearLayout.orientation = LinearLayout.VERTICAL
                            linearLayout.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Créer un TextView pour le titre
                            val titleTextView = TextView(requireContext())
                            titleTextView.text = itemLabel

                            // Ajouter le TextView au LinearLayout
                            linearLayout.addView(titleTextView)

                            // Créer un ImageView pour l'image
                            if (binding.has("pic")) {
                                val imageUrl = binding.getJSONObject("pic").getString("value").replace("http://", "https://")
                                val imageView = ImageView(requireContext())

                                // Utiliser Glide pour charger l'image dans l'ImageView
                                Glide.with(this)
                                    .load(imageUrl)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageView)

                                // Ajouter l'ImageView au LinearLayout
                                linearLayout.addView(imageView)
                            }

                            // Ajouter le LinearLayout au CardView
                            cardView.addView(linearLayout)

                            // Ajouter le CardView à votre HorizontalScrollView
                            if (horizontalScrollView1 != null) {
                                horizontalScrollView1.addView(cardView)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e("SearchFragment", "JSON parsing error: ${e.message}")
            }
        } else {
            Log.e("SearchFragment", "API call failed with code: ${response.code()}")
            // ... (rest of the error handling)
        }
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
    fun Float.dpToPx(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}