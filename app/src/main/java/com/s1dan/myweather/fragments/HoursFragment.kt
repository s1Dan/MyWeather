package com.s1dan.myweather.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.s1dan.myweather.R
import com.s1dan.myweather.adapters.WeatherAdapter
import com.s1dan.myweather.adapters.WeatherModel
import com.s1dan.myweather.databinding.FragmentHoursBinding

class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rcView.adapter = adapter
        val list = listOf(
            WeatherModel( "",
                "12:00",
                "Sunny",
                "25ºC",
                "",
                "",
                "",
                ""
            ),
            WeatherModel( "",
                "13:00",
                "Sunny",
                "27ºC",
                "",
                "",
                "",
                ""
            ),
            WeatherModel( "",
                "14:00",
                "Sunny",
                "35ºC",
                "",
                "",
                "",
                ""
            )
        )
        adapter.submitList(list)

    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}