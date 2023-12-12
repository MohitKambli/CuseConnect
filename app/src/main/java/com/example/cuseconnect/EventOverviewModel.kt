package com.example.cuseconnect

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EventOverviewViewModel : ViewModel() {

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val _eventNames = MutableLiveData<List<String>>()
    val eventNames: LiveData<List<String>> = _eventNames
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events


    init {
        getEventDetails()
    }


    private fun getEventDetails() {
        viewModelScope.launch {
            try {
                val apiKey = "hhOGPrIoMbKV8odSYu8MRaawrffASLBu"
                val city = "Syracuse"
                val response = TicketmasterApi.retrofitService.getEvents(apiKey, city)
                _status.value = "Success: ${response.embedded.events.size} events retrieved"
                _events.value = response.embedded.events
            } catch (e: Exception) {
                Log.e("EventDetails", "Error fetching events", e)
                _status.value = "Failure: ${e.message}"
                _events.value = emptyList() // Set empty list on error
            }
        }
    }

}