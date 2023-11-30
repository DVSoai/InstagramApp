package com.example.instagramapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.instagramapp.R
import com.example.instagramapp.databinding.ActivityMainBinding
import com.example.instagramapp.ui.fragments.HomeFragment
import com.example.instagramapp.ui.fragments.NotificationsFragment
import com.example.instagramapp.ui.fragments.ProfileFragment
import com.example.instagramapp.ui.fragments.SearchFragment
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    internal var selectedFragment: Fragment ?= null

    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
              moveToFragment(HomeFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_search -> {
                moveToFragment(SearchFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
                return@OnItemSelectedListener true

            }
            R.id.nav_notifications -> {
               moveToFragment(NotificationsFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_profile -> {
               moveToFragment(ProfileFragment())
                return@OnItemSelectedListener true
            }
        }

        false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener(onItemSelectedListener)

        moveToFragment(HomeFragment())


    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
}