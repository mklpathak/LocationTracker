package com.example.mukul.locationtracker.view

import com.example.mukul.locationtracker.Data.Cordinates
import com.google.android.gms.maps.GoogleMap

/**
 * Created by mukul on 8/31/18.
 */
interface DATAView {

    fun openSettings()
    fun location(result: List<Cordinates>?, googleMap: GoogleMap)
}