package com.example.mukul.locationtracker.presenter

import android.Manifest
import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.AsyncTask
import android.util.Log
import com.example.mukul.locationtracker.Data.AppDatabase
import com.example.mukul.locationtracker.Data.Cordinates
import com.example.mukul.locationtracker.Data.FetchCordinates
import com.example.mukul.locationtracker.MainActivity
import com.example.mukul.locationtracker.services.LocationUpdater
import com.example.mukul.locationtracker.view.DATAView
import com.google.android.gms.maps.GoogleMap
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.util.*

/**
 * Created by mukul on 8/31/18.
 */
class Presenter  {



    lateinit var cordinatesDatabase: AppDatabase
    lateinit var googleMap:GoogleMap
    var mContext :Context
    var id:Int = -1
    var v: DATAView? = null
    constructor(context: Context, id: Int){
        this.mContext =context
        this.id =id

    }

    fun init(serviceConnection:ServiceConnection){
        Log.e("Hello Pathak","hello 1")

        if (mContext!=null){

            val mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase::class.java).build()
            val fetchcordinates: FetchCordinates = mDb.getCordinates()
            cordinatesDatabase = Room.databaseBuilder(mContext, AppDatabase::class.java, MainActivity.dataBaseName).build()
            Log.e(MainActivity.TAG + "Distance ", getDistanceMeters(12.948363, 77.711954, 13.034790, 77.779163).toString())

            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val rationale = "Please provide location permission so that you can ..."
            val options = Permissions.Options()
                    .setRationaleDialogTitle("Info")
                    .setSettingsDialogTitle("Warning")

            Permissions.check(mContext, permissions, rationale, options, object : PermissionHandler() {
                override fun onGranted() {


                    val intent = Intent(mContext, LocationUpdater::class.java)
                    intent.putExtra("id",id.toString())

                    Log.e("Hello Pathak",id.toString())
                    mContext.startService(intent)
                    Log.e(MainActivity.TAG, "Service Started1")

                    val mIntent = Intent(mContext, LocationUpdater::class.java)
                    mIntent.putExtra("id",id.toString())
                    mContext.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                    Log.e(MainActivity.TAG, "Service Started 2")



                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {

                    if (v!=null)
                    v!!.openSettings()

                }
            })
        }
    }


    fun bind(view: DATAView) {
        this.v = view
    }
    fun unbind() {
        v = null
    }
    fun getDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Long {

        val l1 = Math.toRadians(lat1)
        val l2 = Math.toRadians(lat2)
        val g1 = Math.toRadians(lng1)
        val g2 = Math.toRadians(lng2)

        var dist = Math.acos(Math.sin(l1) * Math.sin(l2) + Math.cos(l1) * Math.cos(l2) * Math.cos(g1 - g2))
        if (dist < 0) {
            dist = dist + Math.PI
        }

        return Math.round(dist * 6378100)
    }


    fun startBackGroundWork(googleMap: GoogleMap){
        this.googleMap=googleMap
        backgroundDatabase().execute()
    }


    inner class backgroundDatabase : AsyncTask<String, String, List<Cordinates>>() {
        override fun doInBackground(vararg p0: String?): List<Cordinates> {
            return cordinatesDatabase.getCordinates().findCordinatesUser(id)
        }

        @SuppressLint("MissingPermission")
        override fun onPostExecute(result: List<Cordinates>?) {
            super.onPostExecute(result)

            if (v!=null && this@Presenter.googleMap!=null)
                v!!.location(result,this@Presenter.googleMap)


        }



    }


}