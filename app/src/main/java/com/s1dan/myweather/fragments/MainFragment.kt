package com.s1dan.myweather.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.s1dan.myweather.DialogManager
import com.s1dan.myweather.MainViewModel
import com.s1dan.myweather.adapters.VpAdapter
import com.s1dan.myweather.adapters.WeatherModel
import com.s1dan.myweather.databinding.FragmentMainBinding
import com.s1dan.myweather.databinding.FragmentMainBinding.inflate
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "fa1ef6faf60147d1b07184457220706"
const val API_DAYS = 3


class MainFragment : Fragment() {

    private lateinit var fLocationClient: FusedLocationProviderClient

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

    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding){
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
                tab, pos -> tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.SearchByNameDialog(requireContext(), object:DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        val canToken = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, canToken.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
        }
    }


    private fun updateCurrentCard() = with(binding) {
        model.LiveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}ºC / ${it.minTemp}ºC"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemp }
            tvCondition.text = it.condition
            tvMaxMin.text = if(it.currentTemp.isEmpty()) "" else maxMinTemp
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

    private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name =  mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.LiveDataList.value = list
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

















