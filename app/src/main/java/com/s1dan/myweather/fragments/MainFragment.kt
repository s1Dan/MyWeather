package com.s1dan.myweather.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.s1dan.myweather.R
import com.s1dan.myweather.databinding.ActivityMainBinding
import com.s1dan.myweather.databinding.FragmentMainBinding
import com.s1dan.myweather.databinding.FragmentMainBinding.inflate

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}