package com.example.mukul.locationtracker.Data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey



/**
 * Created by mukul on 8/31/18.
 */
@Entity(foreignKeys = arrayOf(ForeignKey(entity = Users::class,
        parentColumns = arrayOf("userId"),
        childColumns = arrayOf("userId"),
        onDelete = ForeignKey.CASCADE)))
class Cordinates {
    @PrimaryKey (autoGenerate = true)
    var id: Int = 0

    var longitude: Double? = null
    var lattitude: Double? = null
    var time:String? =null
    var userId: Int? = null




}

