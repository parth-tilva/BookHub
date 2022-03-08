package com.example.bookhub.fragment

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookhub.R
import com.example.bookhub.adapter.FavouritesBookAdapter
import com.example.bookhub.database.BookDao
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavouritesFragment : Fragment() {


    lateinit var adapter: FavouritesBookAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var relativeLayout: RelativeLayout
    lateinit var dao: BookDao
    var bookList:List<BookEntity>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        dao = BookDatabase.getDatabase(requireContext().applicationContext).bookDao()          //Room.databaseBuilder(requireContext(),BookDatabase::class.java,"books-db").build()
        recyclerView= view.findViewById(R.id.rvFavourites)
        progressBar = view.findViewById(R.id.ProgressBar)
        relativeLayout = view.findViewById(R.id.RelativeLayoutPro)
        return view
    }

    fun initBook(){
        lifecycle.coroutineScope.launch {
            bookList = getBookList()
            if(bookList!=null && activity!=null){
                initRV()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initBook()
    }

    fun initRV(){
        relativeLayout.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(activity as Context,2)
        adapter = FavouritesBookAdapter(activity as Context, bookList!!)
        recyclerView.adapter = adapter
        Log.d("fav","ininrv booklist: $bookList")
    }


    suspend fun getBookList(): List<BookEntity> {
       val bookList =  dao.getAllBooks()
        Log.d("fav","ininrv booklist: ${bookList.size}")
        return bookList
    }

}