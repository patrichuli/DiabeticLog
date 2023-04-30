package com.unileon.diabeticlog.controlador

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.vista.DataRecord
import com.unileon.diabeticlog.vista.Home
import com.unileon.diabeticlog.vista.UserProfile

class Principal : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        //variables
        val navView: BottomNavigationView = findViewById(R.id.nav_view)


        //choose which window you want to go to and open that fragment
        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.perfilUsuario -> {
                    val fragment =
                        UserProfile.newInstance()
                    openFragment(fragment)
                    true

                }
                R.id.inicioPantalla -> {
                    val fragment =
                        Home.newInstance()
                    openFragment(fragment)
                    true
                }
                R.id.registro -> {
                    val fragment =
                        DataRecord.newInstance()
                    openFragment(fragment)
                    true
                }
                else -> false
            }
        }

        //fragment which is selected by default
        navView.selectedItemId = R.id.inicioPantalla

    }

    //open the fragment
    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }



}



