package com.s1dan.myweather.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator
import com.s1dan.myweather.MainViewModel
import com.s1dan.myweather.adapters.VpAdapter
import com.s1dan.myweather.adapters.WeatherModel
import com.s1dan.myweather.databinding.FragmentMainBinding
import com.s1dan.myweather.databinding.FragmentMainBinding.bind
import com.s1dan.myweather.databinding.FragmentMainBinding.inflate
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "fa1ef6faf60147d1b07184457220706"
const val API_DAYS = 3


class MainFragment : Fragment() {

    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    private val tList = listOf(
        "Hours",
        "Days"
    )
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
        requestWeatherData("Moscow")
    }

    private fun init() = with(binding){
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
                tab, pos -> tab.text = tList[pos]
        }.attach()
    }


    private fun updateCurrentCard() = with(binding) {
        model.LiveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}Cº/${it.minTemp}Cº"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp
            tvCondition.text = it.condition
            tvMaxMin.text = maxMinTemp

            Picasso.get().load("https:" + it.imageURL).into(imWeather)

        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_SHORT).show()
        }
    }

   private fun checkPermission() {
       if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
           pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
       }
   }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=" +
                API_DAYS +
                "&aqi=no&alerts=no"
        val query = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,

            // слушатель ответа
            {
                result -> parseWeatherData(result)
            },

            // слушатель ошибок
            {
                error -> Log.d("MyLog", "Error: $error")
            }
        )
        query.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")

        val name = mainObject.getJSONObject("location")
            .getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(

                //name
                name,

                //day
                day.getString("day"),

                //text
                day.getJSONObject("day")
                    .getJSONObject("condition").getString("text"),

                // current temp
            "",
                // max temp
                day.getJSONObject("day")
                    .getString("maxtemp_c"),

                // min temp
                day.getJSONObject("day")
                    .getString("mintemp_c"),

                // image URl
                day.getJSONObject("day")
                    .getJSONObject("condition").getString("icon"),

                //hours
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            // location
            mainObject.getJSONObject("location").getString("name"),

            // last update
            mainObject.getJSONObject("current").getString("last_updated"),

            // condition
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),

            // current temp
            mainObject.getJSONObject("current").getString("temp_c"),

            //max temp
            weatherItem.maxTemp,

            //min temp
            weatherItem.minTemp,

            // icon
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),

            // hours
            weatherItem.hours

        )

        // выдаем информацию
        model.LiveDataCurrent.value = item

        Log.d("MyLog", "City: ${item.city}")
        Log.d("MyLog", "time: ${item.time}")
        Log.d("MyLog", "Condition: ${item.condition}")
        Log.d("MyLog", "Temp: ${item.currentTemp}")
        Log.d("MyLog", "URL: ${item.imageURL}")

        Log.d("MyLog", "URL: ${item.maxTemp}")
        Log.d("MyLog", "URL: ${item.minTemp}")
        Log.d("MyLog", "URL: ${item.hours}")



    }



    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}

















