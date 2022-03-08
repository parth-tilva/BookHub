package com.example.bookhub.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.adapter.DashboardRecyclerAdapter
import com.example.bookhub.model.Book
import com.example.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Dashboard : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: DashboardRecyclerAdapter
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

    val ratingComaparator = Comparator<Book>{ book1, book2->
//        if(book1.BookRating.compareTo(book2.BookRating,true)==0){
//            book1.bookName.compareTo(book2.bookName,true)
//        }else
        book1.BookRating.compareTo(book2.BookRating,true)
    }
    val bookInfoList: ArrayList<Book> = arrayListOf<Book>()


//    val bookList = arrayListOf<String>("Harry Potter",
//        "Atomic habits",
//        "Lord of the rings",
//        "Game of Throne",
//        "Karma",
//        "War and peace",
//        "Moby-Dick",
//        "Middlemarch",
//        "Madame Bovary",
//        "Advance minds",
//        "Peaceful warrior",
//        "NoteBook",
//        "pictures",
//        "Gooosebumps",
//        "Adventures of Tintin"
//    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)


        val btn:Button = view.findViewById(R.id.btnCheckInternet)
        progressBar = view.findViewById(R.id.ProgressBar)
        progressLayout = view.findViewById(R.id.ProgressLayout)
        progressLayout.visibility = View.VISIBLE
        btn.visibility = View.GONE
        btn.setOnClickListener {
            if(ConnectionManager().checkConnectivity(requireContext())){
                //Internet is avilable
                val dialog =  AlertDialog.Builder(requireContext())
                dialog.setTitle("Success")
                dialog.setMessage("Internet Connection Found")
                dialog.setPositiveButton("OK"){ _, _ ->
                    //do nothing
                    Toast.makeText(requireContext(),"OK Clicked", Toast.LENGTH_LONG).show()
                }
                dialog.setNegativeButton("Cancel"){ _,_->
                    //do nothing
                    Toast.makeText(context,"Cancel Clicked", Toast.LENGTH_LONG).show()

                }
                dialog.create()
                dialog.show()


            } else{
                val dialog =  AlertDialog.Builder(requireContext())
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection not Found")
                dialog.setPositiveButton("OK"){ _, _ ->
                    //do nothing
                    Toast.makeText(requireContext(),"OK Clicked", Toast.LENGTH_LONG).show()
                }
                dialog.setNegativeButton("Cancel"){ _,_->
                    //do nothing
                    Toast.makeText(context,"Cancel Clicked", Toast.LENGTH_LONG).show()

                }
                dialog.create()
                dialog.show()
            }
        }


        recyclerView = view.findViewById(R.id.rvDashboard)
        layoutManager = LinearLayoutManager(activity)



        val queue = Volley.newRequestQueue(requireContext())
        val url = "http://13.235.250.119/v1/book/fetch_books/"


        if(ConnectionManager().checkConnectivity(requireContext())){
            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener{
                    //Here we will handle the response
                    //println("response is $it")
                    try {
                        progressLayout.visibility = View.GONE
                        val success = it.getBoolean("success")
                        if(success){
                            val data = it.getJSONArray("data")
                            for(i in 0 until data.length()){
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image"),
                                )
                                bookInfoList.add(bookObject)
                            }
                            if(activity!=null){
                                recyclerAdapter = DashboardRecyclerAdapter(activity as Context,bookInfoList)// activity as Context or requireContext()
                                recyclerView.adapter = recyclerAdapter
                                recyclerView.layoutManager = layoutManager
                            }

                        } else{
                            Toast.makeText(requireContext(),"some Error in Parsing ",Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException){
                        Toast.makeText(requireContext(),"some Error in json $e ",Toast.LENGTH_LONG).show()

                    }

                },
                Response.ErrorListener{
                    //Here we will handle the errors
                    println("Error is $it")
                    Toast.makeText(requireContext(),"volley error occur $it ",Toast.LENGTH_LONG).show()

                }) {
                override fun getHeaders(): MutableMap<String, String> {// hashMap is derived from mutable map
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "f56132fd80ca31" // teacher's 9bf534118365f1
                    return headers
                }
            }
            queue.add(jsonObjectRequest)


        } else{
            val dialog =  AlertDialog.Builder(requireContext())
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Setting"){ _, _ ->
                //do nothing
                //Toast.makeText(requireContext(),"OK Clicked", Toast.LENGTH_LONG).show()
                val settingIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit  "){ _,_->
                //do nothing
                //Toast.makeText(context,"Cancel Clicked", Toast.LENGTH_LONG).show()

                ActivityCompat.finishAffinity(requireActivity()) // activity as Activity

            }
            dialog.create()
            dialog.show()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList ,ratingComaparator)
            bookInfoList.reverse()
        }
        recyclerAdapter.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }


}