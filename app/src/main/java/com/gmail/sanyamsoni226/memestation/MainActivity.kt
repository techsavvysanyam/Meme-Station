package com.gmail.sanyamsoni226.memestation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var memeAdapter: MemeAdapter
    private lateinit var memeList: MutableList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Variable for pagination
    private var isLoading = false
    private var currentPage = 1
    private val memesPerPage = 10 // Adjust according to API if pagination supported

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.memeRecyclerView)
        progressBar = findViewById(R.id.memeLoadBar)

        memeList = mutableListOf()
        memeAdapter = MemeAdapter(memeList)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = memeAdapter

        // Initial load of memes
        loadMemes()

        // Setup infinite scroll
        setupInfiniteScroll()
    }

    private fun setupInfiniteScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more memes when reaching the end of the list
                    loadMemes()
                }
            }
        })
    }

    private fun loadMemes() {
        isLoading = true
        progressBar.visibility = View.VISIBLE

        // Fetch a single meme from the API
        val url = "https://meme-api.com/gimme"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Extract the meme URL from the response
                    val memeUrl = response.getString("url")

                    // Add the new meme to the list
                    memeAdapter.addMemes(listOf(memeUrl)) // Add as a single-item list

                    // Update loading status
                    isLoading = false
                    progressBar.visibility = View.GONE

                } catch (e: JSONException) {
                    e.printStackTrace()
                    isLoading = false
                    progressBar.visibility = View.GONE
                }
            },
            { error ->
                // Handle error
                Log.e("API Error", "An error occurred: ${error.message}")
                isLoading = false
                progressBar.visibility = View.GONE
            }
        )

        // Add the request to the Volley request queue
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

}
