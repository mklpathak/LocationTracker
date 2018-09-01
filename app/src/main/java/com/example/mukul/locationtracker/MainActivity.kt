package com.example.mukul.locationtracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.mukul.locationtracker.Data.Cordinates
import com.example.mukul.locationtracker.presenter.Presenter
import com.example.mukul.locationtracker.services.LocationUpdater
import com.example.mukul.locationtracker.view.DATAView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*


/**
 * Reference: https://github.com/googlesamples/android-play-location/tree/master/LocationUpdates
 */

class MainActivity : AppCompatActivity(), OnMapReadyCallback ,DATAView {

    lateinit var dataPresenter: Presenter
    private lateinit var mMap: GoogleMap


    var updateText :String = ""
    private var mRequestingLocationUpdates: Boolean? = null

    var  id :Int = -1
    var name:String = ""


    val cordinateReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            Log.e("BASjhbdhi","hcbuhcbwu")

            var long = p1!!.getStringExtra("Longitude").toDouble()
            var latt = p1.getStringExtra("Lattitude").toDouble()
            var time = p1.getStringExtra("Time")


                mMap.addMarker(MarkerOptions().position(LatLng(p1.getStringExtra("Lattitude").toDouble(),
                        p1.getStringExtra("Longitude").toDouble())).title(p1.getStringExtra("Time")))


            update.text = updateText+ "Lattitude : "+latt+" , "+"Longitude : "+long +" On "+time+"\n"





        }


    }



    var mBounded: Boolean = false
    var mService: LocationUpdater? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        id = intent.getStringExtra("id").toInt()
        name = intent.getStringExtra("name")

        Log.e("Hello Pathak","hello")

        Log.e(" Userid",id.toString())
        dataPresenter = Presenter(this,id)
        dataPresenter.bind(this)
        dataPresenter.init(serviceConnection)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        //Start the background data fetch from database

        this.registerReceiver(cordinateReceiver, IntentFilter("Cordinates"))

        var st = 1;

        start.setOnClickListener {
            if (st == 1){
            stopService(Intent(this,LocationUpdater::class.java))
            start.setText("Start Location Tracking")
            st=0}
            else {
                st=1
                dataPresenter.init(serviceConnection)
                start.setText("Stop Location Tracking")
                val intent = Intent(this, LocationUpdater::class.java)
                intent.putExtra("id",id.toString())

                Log.e("Hello Pathak",id.toString())
                startService(intent)
                val mIntent = Intent(this, LocationUpdater::class.java)
                mIntent.putExtra("id",id.toString())

                bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)


            }

        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                applicationContext.getSharedPreferences("usersLoggedIn", 0).edit().clear().commit()


                stopService(Intent(this,LocationUpdater::class.java))
                val intentID : Intent = Intent(this@MainActivity,SignIn::class.java)
                startActivity(intentID)

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()


        if (checkPermissions()) {
            val mIntent = Intent(this, LocationUpdater::class.java)
            mIntent.putExtra("id",id.toString())

            bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        }

        this.registerReceiver(cordinateReceiver, IntentFilter("Cordinates"))

    }

    override fun onPause() {
        super.onPause()

        if (mBounded) {
            unbindService(serviceConnection);
            mBounded = false;
        }
    }

    override fun onStart() {
        super.onStart()
        val mIntent = Intent(this, LocationUpdater::class.java)
        mIntent.putExtra("id",id.toString())

        bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (mBounded) {
            unbindService(serviceConnection);
            mBounded = false;
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(cordinateReceiver)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.960977, 77.696700), 17f));

            val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(12.960977, 77.696700))      // Sets the center of the map to location user
                    .zoom(17f)                   // Sets the zoom
                    .bearing(90f)                // Sets the orientation of the camera to east
                    .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                    .build()              // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        dataPresenter.startBackGroundWork(mMap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.e(TAG, "User agreed to make required location settings changes.")

                    startService(intent)
                    Log.e(MainActivity.TAG, "Service Started1")

                    val mIntent = Intent(this@MainActivity, LocationUpdater::class.java)
                    bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                    Log.e(MainActivity.TAG, "Service Started 2")

                }
                Activity.RESULT_CANCELED -> {

                    Toast.makeText(this, "User chose not to make required location settings changes.", Toast.LENGTH_LONG).show()
                    mRequestingLocationUpdates = false
                }
            }// Nothing to do. startLocationupdates() gets called in onResume again.
        }
    }

    override  fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    override fun location(result: List<Cordinates>?, googleMap: GoogleMap) {

        mMap = googleMap

        if (result != null && result.size > 0) {

            var cordinates1: Cordinates = result.get(0)
            mMap.isMyLocationEnabled = true
            mMap.addMarker(MarkerOptions().position(LatLng(cordinates1.lattitude!!, cordinates1.longitude!!)).title(cordinates1.time))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(cordinates1.lattitude!!, cordinates1.longitude!!)))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(cordinates1.lattitude!!, cordinates1.longitude!!), 20.0f))
            for (i in 1..result.size - 1) {

                if (getDistanceMeters(cordinates1.lattitude!!, cordinates1.longitude!!,
                        result.get(i).lattitude!!, result.get(i).longitude!!) > 10) {
                    //display cordinates
                    mMap.addMarker(MarkerOptions().position(LatLng(result.get(i).lattitude!!, result.get(i).longitude!!)).title(result.get(i).time))
                    update.text = updateText+ "Lattitude : "+result.get(i).lattitude!!+" , "+"Longitude : "+result.get(i).longitude!! +" on "+result.get(i).time+"\n"
                    cordinates1 = result.get(i)

                    val cameraPosition = CameraPosition.Builder()
                            .target(LatLng(result.get(i).lattitude!!,result.get(i).longitude!!))      // Sets the center of the map to location user
                            .zoom(20f)                   // Sets the zoom
                            .bearing(90f)                // Sets the orientation of the camera to east
                            .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                            .build()              // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                }


            }

            //result.text = resultLocation


        }

    }


    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Toast.makeText(this@MainActivity, "Service is disconnected", Toast.LENGTH_SHORT).show()
            mBounded = false
            mService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Toast.makeText(this@MainActivity, "Service is connected", Toast.LENGTH_SHORT).show()
            mBounded = true
            val mLocalBinder = service as LocationUpdater.MyLocalBinder
            mService = mLocalBinder.getService()
        }
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



    companion object {

         val TAG = MainActivity::class.simpleName

        // location updates interval - 10sec
        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        // fastest updates interval - 5 sec
        // location updates will be received if another app is requesting the locations
        // than your app can handle
        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

        private val REQUEST_CHECK_SETTINGS = 100
        val dataBaseName = "CordinatesDB"

    }

}
