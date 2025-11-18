package com.krishna.varunaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.R
import com.krishna.varunaapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userRole: String = "GeneralUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        loadUserRole()

        val nav = findNavController(R.id.nav_host_fragment)

        // ------ BOTTOM NAVIGATION ------
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.menu_dashboard -> nav.navigate(R.id.dashboardFragment)

                R.id.menu_water -> nav.navigate(R.id.waterQualityFragment)

                R.id.menu_reports -> {
                    val intent = Intent(this, VillageListActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_profile -> nav.navigate(R.id.profileFragment)
            }
            true
        }

        // ------ DRAWER MENU ------
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_profile -> nav.navigate(R.id.profileFragment)

                R.id.nav_help -> {
                    if (userRole == "HealthReport") {
                        nav.navigate(R.id.helpRequestsFragment)
                    } else {
                        nav.navigate(R.id.helpRequestsFragment)
                    }
                }

                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role") ?: "GeneralUser"

                val menu = binding.navigationView.menu
                val helpItem = menu.findItem(R.id.nav_help)

                helpItem.title =
                    if (userRole == "HealthReport") "Manage Help Requests"
                    else "Help"
            }
    }
}


//package com.krishna.varunaapp.activities
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.GravityCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.R
//import com.krishna.varunaapp.databinding.ActivityMainBinding
//import com.krishna.varunaapp.fragments.*
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
//    private var userRole: String = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationOnClickListener {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }
//
//        // Default fragment
//        replaceFragment(DashboardFragment())
//
//        // Load user role
//        loadUserRole()
//
//        // ---------- BOTTOM NAVIGATION ----------
//        binding.bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//
//                R.id.menu_dashboard -> replaceFragment(DashboardFragment())
//
//                R.id.menu_water -> replaceFragment(WaterQualityFragment())
//
//                // CHANGED: Navigate to VillageListActivity instead of single village
//                R.id.menu_reports -> {
//                    val intent = Intent(this, VillageListActivity::class.java)
//                    startActivity(intent)
//                }
//
//                R.id.menu_profile -> replaceFragment(ProfileFragment())
//            }
//            true
//        }
//
//        // ---------- DRAWER NAVIGATION ----------
//        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//
//                R.id.nav_profile -> replaceFragment(ProfileFragment())
//
//                R.id.nav_help -> {
//                    if (userRole == "HealthReport") {
//                        replaceFragment(HelpRequestsFragment()) // Admin Help view
//                    } else {
//                        replaceFragment(ReportsFragment()) // Regular users see Reports
//                    }
//                }
//
//                R.id.nav_logout -> {
//                    auth.signOut()
//                    startActivity(Intent(this, LoginActivity::class.java))
//                    finishAffinity()
//                }
//            }
//
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener { doc ->
//                userRole = doc.getString("role") ?: "GeneralUser"
//
//                val menu = binding.navigationView.menu
//                val helpItem = menu.findItem(R.id.nav_help)
//
//                helpItem.title =
//                    if (userRole == "HealthReport") "Manage Help Requests"
//                    else "Help"
//            }
//    }
//
//    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(binding.container.id, fragment)
//            .commit()
//    }
//}


//package com.krishna.varunaapp.activities
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.GravityCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.R
//import com.krishna.varunaapp.databinding.ActivityMainBinding
//import com.krishna.varunaapp.fragments.*
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
//    private var userRole: String = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationOnClickListener {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }
//
//        // Default fragment
//        replaceFragment(DashboardFragment())
//
//        // Load user role
//        loadUserRole()
//
//        // ---------- BOTTOM NAVIGATION ----------
//        binding.bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//
//                R.id.menu_dashboard -> replaceFragment(DashboardFragment())
//
//                R.id.menu_water -> replaceFragment(WaterQualityFragment())
//
//                // -------- CHANGED HERE --------
//                R.id.menu_reports -> {
//                    val intent = Intent(this, VillageActivity::class.java)
//                    intent.putExtra("villageName", "DefaultVillage")
//                    startActivity(intent)
//                }
//
//                R.id.menu_profile -> replaceFragment(ProfileFragment())
//            }
//            true
//        }
//
//        // ---------- DRAWER NAVIGATION ----------
//        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//
//                R.id.nav_profile -> replaceFragment(ProfileFragment())
//
//                R.id.nav_help -> {
//                    if (userRole == "HealthReport") {
//                        replaceFragment(HelpRequestsFragment()) // Admin Help view
//                    } else {
//                        replaceFragment(ReportsFragment()) // Regular users see Reports
//                    }
//                }
//
//                R.id.nav_logout -> {
//                    auth.signOut()
//                    startActivity(Intent(this, LoginActivity::class.java))
//                    finishAffinity()
//                }
//            }
//
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener { doc ->
//                userRole = doc.getString("role") ?: "GeneralUser"
//
//                val menu = binding.navigationView.menu
//                val helpItem = menu.findItem(R.id.nav_help)
//
//                helpItem.title =
//                    if (userRole == "HealthReport") "Manage Help Requests"
//                    else "Help"
//            }
//    }
//
//    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(binding.container.id, fragment)
//            .commit()
//    }
//}


//package com.krishna.varunaapp.activities
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.GravityCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.R
//import com.krishna.varunaapp.databinding.ActivityMainBinding
//import com.krishna.varunaapp.fragments.*
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
//    private var userRole: String = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationOnClickListener {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }
//
//        // Default fragment
//        replaceFragment(DashboardFragment())
//
//        // Load user role and adjust drawer options
//        loadUserRole()
//
//        // Bottom navigation
//        binding.bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.menu_dashboard -> replaceFragment(DashboardFragment())
//                R.id.menu_water -> replaceFragment(WaterQualityFragment())
//                R.id.menu_reports -> replaceFragment(ReportsFragment())
//                R.id.menu_profile -> replaceFragment(ProfileFragment())
//            }
//            true
//        }
//
//        // Drawer navigation
//        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_profile -> replaceFragment(ProfileFragment())
//                R.id.nav_help -> {
//                    if (userRole == "HealthReport") {
//                        replaceFragment(HelpRequestsFragment()) // Admin Help Requests view
//                    } else {
//                        replaceFragment(ReportsFragment()) // Regular users see Reports
//                    }
//                }
//                R.id.nav_logout -> {
//                    auth.signOut()
//                    startActivity(Intent(this, LoginActivity::class.java))
//                    finishAffinity()
//                }
//            }
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener { doc ->
//                userRole = doc.getString("role") ?: "GeneralUser"
//
//                // For admins, show "Help Requests" option
//                val menu = binding.navigationView.menu
//                val helpItem = menu.findItem(R.id.nav_help)
//                helpItem.title = if (userRole == "HealthReport") "Manage Help Requests" else "Help"
//            }
//    }
//
//    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(binding.container.id, fragment)
//            .commit()
//    }
//}
//
//
////package com.krishna.varunaapp.activities
////
////import android.os.Bundle
////import androidx.appcompat.app.AppCompatActivity
////import androidx.core.view.GravityCompat
////import com.krishna.varunaapp.R
////import com.krishna.varunaapp.databinding.ActivityMainBinding
////import com.krishna.varunaapp.fragments.*
////
////class MainActivity : AppCompatActivity() {
////
////    private lateinit var binding: ActivityMainBinding
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        binding = ActivityMainBinding.inflate(layoutInflater)
////        setContentView(binding.root)
////
////        // Set up toolbar and drawer
////        setSupportActionBar(binding.toolbar)
////        binding.toolbar.setNavigationOnClickListener {
////            binding.drawerLayout.openDrawer(GravityCompat.START)
////        }
////
////        // Load default fragment
////        replaceFragment(DashboardFragment())
////
////        // Bottom navigation
////        binding.bottomNav.setOnItemSelectedListener { item ->
////            when (item.itemId) {
////                R.id.menu_dashboard -> replaceFragment(DashboardFragment())
////                R.id.menu_water -> replaceFragment(WaterQualityFragment())
////                R.id.menu_reports -> replaceFragment(ReportsFragment())
////                R.id.menu_profile -> replaceFragment(ProfileFragment())
////            }
////            true
////        }
////
////        // Drawer navigation
////        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
////            when (menuItem.itemId) {
////                R.id.nav_profile -> replaceFragment(ProfileFragment())
////                R.id.nav_help -> replaceFragment(ReportsFragment()) // or HelpFragment
////                R.id.nav_logout -> finishAffinity() // closes app (you can add logout logic)
////            }
////            binding.drawerLayout.closeDrawer(GravityCompat.START)
////            true
////        }
////    }
////
////    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
////        supportFragmentManager.beginTransaction()
////            .replace(binding.container.id, fragment)
////            .commit()
////    }
////}
