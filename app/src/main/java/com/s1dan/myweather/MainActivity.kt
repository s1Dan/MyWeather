package com.s1dan.myweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.s1dan.myweather.databinding.ActivityMainBinding
import com.s1dan.myweather.fragments.MainFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance())
            .commit()
    }
}