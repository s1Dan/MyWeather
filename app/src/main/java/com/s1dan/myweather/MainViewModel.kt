package com.s1dan.myweather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    val LiveDataCurrent = MutableLiveData<String>()
    val LiveDataList = MutableLiveData<List<String>>()

}