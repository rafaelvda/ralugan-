package com.ralugan.raluganplus.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!



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