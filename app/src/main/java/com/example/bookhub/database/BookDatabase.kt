package com.example.bookhub.database

import android.content.Context
import android.provider.ContactsContract
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [BookEntity::class], version = 1)
abstract class BookDatabase: RoomDatabase() {
    abstract fun bookDao(): BookDao
    companion object{
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase {
            //if the INSTANCE is Not null, then return it,
            //if it is, then create the database
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "Book-db"
                ).build()
                INSTANCE = instance
                instance
            }

        }

    }

}


