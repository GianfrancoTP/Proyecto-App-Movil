package com.example.entrega1proyecto.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ListBDD::class, ItemBDD::class],version = 3)
abstract class Database: RoomDatabase() {
    abstract fun ListDao(): ListDao
}