package com.gmail.sanyamsoni226.memestation

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gmail.sanyamsoni226.memestation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var currentImageUrl: String? = null
    private lateinit var binding: ActivityMainBinding //binding variable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) //1
        setContentView(binding.root) //2
        loadMeme() //3
    }
    private fun loadMeme(){
        binding.memeLoadBar.visibility = View.VISIBLE
        // Instantiate the RequestQueue.
        currentImageUrl = "https://meme-api.com/gimme"

        // Requesting a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, currentImageUrl, null,
            { response ->
                currentImageUrl = response.getString("url")
                Glide.with(this)
                    .load(currentImageUrl)
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.memeLoadBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.memeLoadBar.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.memeImage)
            },
            {
                Toast.makeText(this, "No internet connection 🔄️", Toast.LENGTH_LONG).show()
            })

        // Added the request to the RequestQueue.
        MyJodAPI.getInstance(this).addToRequestQueue(jsonObjectRequest) //ENCRYPTED - HINT "JOD"
        //Copyright 2023 sanyamsoni226@gmail.com
    }
    fun shareMeme(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Checkout this meme!\n$currentImageUrl")
        val chooser = Intent.createChooser(intent, "Share this meme using")
        startActivity(chooser)
    }
    fun nextMeme(view: View) {
        loadMeme()
    }
}