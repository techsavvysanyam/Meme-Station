package com.gmail.sanyamsoni226.memestation

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var logoAnimator: LogoAnimator
    private lateinit var memeAdapter: MemeAdapter
    private var isLoading = false
    private val loadedMemes = mutableSetOf<String>() // Use a Set to store loaded memes to avoid duplicates
    private val memeBatchSize = 10

    // UI components
    private lateinit var progressBar: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var noInternetImage: View
    private lateinit var retryButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        progressBar = findViewById(R.id.progressBar)
        val logo = findViewById<View>(R.id.logo)
        recyclerView = findViewById(R.id.memeRecyclerView)
        noInternetImage = findViewById(R.id.noInternetImage)
        retryButton = findViewById(R.id.retryButton)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        memeAdapter = MemeAdapter(loadedMemes.toMutableList()) // Convert Set to MutableList for adapter
        recyclerView.adapter = memeAdapter

        // Instantiate the animation class
        logoAnimator = LogoAnimator(this)

        // Perform the animation on the logo
        logoAnimator.animateLogo(logo, ::loadMemes, ::setupInfiniteScroll)

        // Retry button functionality
        retryButton.setOnClickListener {
            reloadApplication()
        }
    }

    private fun reloadApplication() {
        finish()
        startActivity(intent) // Re-launches the activity
    }

    private fun loadMemes() {
        if (!isNetworkAvailable()) {
            showNoInternetUI()
            return
        }

        hideNoInternetUI()
        isLoading = true
        progressBar.visibility = View.VISIBLE

        val url = "https://meme-api.com/gimme/$memeBatchSize"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val memesArray = response.getJSONArray("memes")
                    val newMemes = mutableListOf<String>()

                    for (i in 0 until memesArray.length()) {
                        val memeUrl = memesArray.getJSONObject(i).getString("url")

                        // Only add meme if it's not already in the set
                        if (loadedMemes.add(memeUrl)) {
                            newMemes.add(memeUrl)
                        }
                    }

                    if (newMemes.isNotEmpty()) {
                        memeAdapter.addMemes(newMemes)
                    }

                    isLoading = false
                    progressBar.visibility = View.GONE
                } catch (e: JSONException) {
                    e.printStackTrace()
                    isLoading = false
                    progressBar.visibility = View.GONE
                }
            },
            { error ->
                Log.e("API Error", "An error occurred: ${error.message}")
                isLoading = false
                progressBar.visibility = View.GONE
            }
        )

        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }

    private fun showNoInternetUI() {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        noInternetImage.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }

    private fun hideNoInternetUI() {
        noInternetImage.visibility = View.GONE
        retryButton.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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
                    loadMemes()
                }
            }
        })
    }
}

