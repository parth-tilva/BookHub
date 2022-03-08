package com.example.bookhub.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.example.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var btnAddtofav: Button
    lateinit var txtBookDescription: TextView
    lateinit var progressBar: ProgressBar
    lateinit var progressbarLayout: RelativeLayout
    lateinit var db : BookDatabase

    var bookId: String? = "100"





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)
         txtBookName  = findViewById(R.id.txtBookName)
         txtBookAuthor = findViewById(R.id.txtBookAuthor)
         txtBookPrice = findViewById(R.id.txtBookPrice)
         txtBookRating = findViewById(R.id.txtBookRating)
         imgBookImage  = findViewById(R.id.imgBookImage)
        btnAddtofav = findViewById(R.id.btnAddtoFavorite)
        txtBookDescription = findViewById(R.id.txtBookDescription)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressbarLayout = findViewById(R.id.rLayoutProgressBar)
        progressbarLayout.visibility = View.VISIBLE


        db = BookDatabase.getDatabase(this@DescriptionActivity)
        title = "Description"

        if(intent!=null){
            bookId = intent.getStringExtra("book_id")
            //Log.d("Des","id: of book: $bookId")
        }else{
            finish()
            Toast.makeText(this@DescriptionActivity,"unknown error occurred!!",Toast.LENGTH_LONG).show()
        }
        if(bookId == "100"){
            finish()
            Toast.makeText(this@DescriptionActivity,"unknown error occurred!!",Toast.LENGTH_LONG).show()
        }


        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)

        if(ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonParams,
                Response.Listener {
                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            val bookJsonobject = it.getJSONObject("book_data")
                            progressBar.visibility = View.GONE
                            progressbarLayout.visibility = View.GONE
                            val bookImageUrl = bookJsonobject.getString("image")
                            Picasso.get().load(bookImageUrl)
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonobject.getString("name")
                            txtBookAuthor.text = bookJsonobject.getString("author")
                            txtBookDescription.text = bookJsonobject.getString("description")
                            txtBookRating.text = bookJsonobject.getString("rating")
                            txtBookPrice.text = bookJsonobject.getString("price")
                            val id = bookJsonobject.getString("book_id")

                            val bookEntity = BookEntity(book_id = id.toInt(),
                                bookName = txtBookName.text.toString(),
                                bookAuthor = txtBookAuthor.text.toString(),
                                bookPrice = txtBookPrice.text.toString(),
                                bookRating = txtBookRating.text.toString(),
                                bookDesc = txtBookDescription.text.toString(),
                                bookImage = bookImageUrl,
                            )

                            fun setRemove(){
                                btnAddtofav.text = "Remove "
                                val color = ContextCompat.getColor(this,R.color.colorFavourite)
                                btnAddtofav.setBackgroundColor(color)
                            }

                            fun setAdd(){
                                btnAddtofav.text = "Add"
                                val color = ContextCompat.getColor(this,R.color.purple_500)
                                btnAddtofav.setBackgroundColor(color)
                            }



                            lifecycleScope.launch {
                                val  isFav = checkIsFavourite(db,bookEntity)
                                if(isFav)
                                    setRemove()
                                else
                                    setAdd()
                            }

                            btnAddtofav.setOnClickListener {
                                lifecycleScope.launch {
                                    val isFav = checkIsFavourite(db,bookEntity)
                                    if(isFav){
                                        removeBook(db,bookEntity)
                                        setAdd()
                                    }else{
                                        insertBook(db,bookEntity)
                                        setRemove()
                                    }
                                }
                            }


                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "some Error in Parsing ",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "some Error in json $e ",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                },
                Response.ErrorListener {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "volley error occur $it ",
                        Toast.LENGTH_LONG
                    ).show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "f56132fd80ca31"
                    return headers
                }
            }

            queue.add(jsonRequest)
        } else{
            val dialog =  AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Setting"){ _, _ ->

                val settingIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                startActivity(settingIntent)
                this.finish()
            }
            dialog.setNegativeButton("Exit  "){ _,_->
                ActivityCompat.finishAffinity(this@DescriptionActivity) // activity as Activity
            }
            dialog.create()
            dialog.show()
        }

    }





    suspend fun checkIsFavourite(db: BookDatabase,bookEntity: BookEntity):Boolean{
        val book:BookEntity? = db.bookDao().getBookById(bookEntity.book_id)// imp to add
        Log.d("fav","book is: $book")
        return book!= null
    }


    suspend fun removeBook(db: BookDatabase,bookEntity: BookEntity){
            db.bookDao().DeleteBook(bookEntity)
    }

    suspend fun insertBook(db: BookDatabase, bookEntity: BookEntity){
            db.bookDao().insertBook(bookEntity)
    }
}