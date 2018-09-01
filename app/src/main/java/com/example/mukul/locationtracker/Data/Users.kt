package com.example.mukul.locationtracker.Data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey



/**
 * Created by mukul on 9/1/18.
 */
@Entity(indices = arrayOf(Index(value = "email", unique = true)))
class Users {

    @PrimaryKey (autoGenerate = true)
    var userId: Int? = null

    var email: String? = null
    var password: String? = null
    var name:String? =null

}