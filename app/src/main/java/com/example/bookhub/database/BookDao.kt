package com.example.bookhub.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(bookEntity: BookEntity)

    @Delete
    suspend fun DeleteBook(bookEntity: BookEntity)

    @Query("select * from books ")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("Select * from books where book_id = :bookId")
    suspend fun getBookById(bookId: Int): BookEntity

}
