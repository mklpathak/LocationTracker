package com.example.mukul.locationtracker.Data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase



/**
 * Created by mukul on 8/31/18.
 */

@Database(entities = arrayOf(Cordinates::class,Users::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCordinates(): FetchCordinates
}