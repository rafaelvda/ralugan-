package com.ralugan.raluganplus

import android.os.Bundle
import android.view.Menu
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.ralugan.raluganplus.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = createAppBarConfiguration()

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Mettez votre code ici pour gérer la visibilité des éléments du menu
        updateMenuVisibility(navView.menu)
    }

    private fun createAppBarConfiguration(): AppBarConfiguration {
        val destinationSet = mutableSetOf(
            R.id.navigation_home, R.id.navigation_search, R.id.navigation_download
        )

        if (userIsLoggedIn()) {
            destinationSet.add(R.id.navigation_profile)
        } else {
            destinationSet.add(R.id.navigation_login)
        }

        return AppBarConfiguration(destinationSet)
    }

    private fun updateMenuVisibility(menu: Menu) {
        // Lorsque l'état de connexion change
        if (userIsLoggedIn()) {
            menu.findItem(R.id.navigation_profile)?.isVisible = true
            menu.findItem(R.id.navigation_login)?.isVisible = false
        } else {
            menu.findItem(R.id.navigation_profile)?.isVisible = false
            menu.findItem(R.id.navigation_login)?.isVisible = true
        }
    }

    private fun userIsLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser != null
    }
}



