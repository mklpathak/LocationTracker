package com.example.mukul.locationtracker.Data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query


/**
 * Created by mukul on 8/31/18.
 */


@Dao
interface FetchCordinates {
    @Query("SELECT * FROM Cordinates")
    fun getAll(): List<Cordinates>

    @Query("SELECT * FROM Users")
    fun getUsers(): List<Users>
    @Insert
    fun insertAll(cordinates: Cordinates)

    @Delete
    fun delete(cordinates: Cordinates)

    @Query("SELECT * FROM Cordinates WHERE userId= :arg0")
    fun findCordinatesUser(userId: Int): List<Cordinates>

    @Insert
    fun insertUser(users: Users)

    @Query("SELECT * FROM Users WHERE email= :arg0 ")
    fun getUsersByEmail(email:String): Users
}