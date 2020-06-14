package com.example.entrega1proyecto.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ListBDD::class, ItemBDD::class, UserBBDD::class],version = 1)
abstract class Database: RoomDatabase() {
    abstract fun ListDao(): ListDao
}