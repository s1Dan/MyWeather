package com.s1dan.myweather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.s1dan.myweather.adapters.WeatherModel

class MainViewModel : ViewModel() {
    val LiveDataCurrent = MutableLiveData<WeatherModel>()
    val LiveDataList = MutableLiveData<List<WeatherModel>>()

}