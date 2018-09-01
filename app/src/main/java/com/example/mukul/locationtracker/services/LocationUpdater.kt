package com.example.mukul.locationtracker.services

import android.app.Service
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.mukul.locationtracker.Data.AppDatabase
import com.example.mukul.locationtracker.Data.Cordinates
import com.example.mukul.locationtracker.Data.FetchCordinates
import com.google.android.gms.maps.GoogleMap
import java.text.SimpleDateFormat
import java.util.*




class LocationUpdater : Service()  {
    var seriviceState = 0
    lateinit var mMap : GoogleMap



    private val TAG = "MUKULGPS"
    private var mLocationManager: LocationManager? = null
    private val LOCATION_INTERVAL : Long= 1000
    private val LOCATION_DISTANCE = 100f
    lateinit var cordinatesDatabase : AppDatabase
    var id:Int = -1
    var mBinder: IBinder = MyLocalBinder()

    var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))

    override fun onCreate() {
        seriviceState = 1



        val mDb = Room.inMemoryDatabaseBuilder(this, AppDatabase::class.java).build() // Get an Instance of Database class //defined above
        val fetchcordinates :FetchCordinates = mDb.getCordinates()


        cordinatesDatabase = Room.databaseBuilder(applicationContext,AppDatabase::class.java,LocationUpdater.dataBaseName).build()

        super.onCreate()
        Log.e(TAG, "onCreate")
        if (id != -1)
        initializeLocation(id)


    }

    override fun onBind(intent: Intent): IBinder? {
        seriviceState = 1

        id=  intent!!.getStringExtra("id").toInt()

        Log.e(" Userid",id.toString()+"mukul")
        if (id!= -1)
            initializeLocation(id)


        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        seriviceState = 1




        if (id!= -1)
        initializeLocation(id)

        return Service.START_STICKY
    }


    private fun initializeLocation(id: Int) {
        Log.e(TAG, "initializeLocation")
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        try {
            if (mLocationManager!=null)
                mLocationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1])


        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            if (mLocationManager!=null)
                mLocationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

    }

    companion object {

        private val TAG = LocationUpdater::class.simpleName

        val dataBaseName = "CordinatesDB"


    }

    inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener " + provider)
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            if (id!=-1 && seriviceState==1) {
                Log.e(TAG, "onLocationChanged: " + location)
                val calender = Calendar.getInstance()


                calender.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"))
                val dateformat = SimpleDateFormat("hh:mm aa")
                val datetime = dateformat.format(calender.getTime())


                mLastLocation.set(location)

                var cordinates = Cordinates()
                cordinates.longitude = location.longitude
                cordinates.lattitude = location.latitude
                cordinates.time = datetime.toString()
                cordinates.userId = id

                Thread(Runnable {
                    kotlin.run {
                        cordinatesDatabase.getCordinates().insertAll(cordinates)
                        for (a: Cordinates in cordinatesDatabase.getCordinates().getAll()) {
                            Log.e(LocationUpdater.TAG, a.lattitude.toString())
                        }
                    }
                }).start()


                //Update marker on map
                val broadCastLocation = Intent("Cordinates")

                broadCastLocation.putExtra("Longitude", cordinates.longitude.toString())
                broadCastLocation.putExtra("Lattitude", cordinates.lattitude.toString())
                broadCastLocation.putExtra("Time", cordinates.time)
                broadCastLocation.putExtra("id", cordinates.userId.toString())



                sendBroadcast(broadCastLocation)
            }

        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: " + provider)
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: " + provider)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: " + provider)
            //Toast.makeText(this@LocationUpdater,"onStatusChanged",Toast.LENGTH_SHORT).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seriviceState = 0
        Log.e("Hello","Suicide")

    }

    inner class MyLocalBinder : Binder() {
        fun getService() : LocationUpdater {
            return this@LocationUpdater
        }
    }


}
