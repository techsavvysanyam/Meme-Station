package com.gmail.sanyamsoni226.memestation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gmail.sanyamsoni226.memestation.databinding.ItemMemeBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MemeAdapter(private val memes: MutableList<String>) : RecyclerView.Adapter<MemeAdapter.MemeViewHolder>() {

    inner class MemeViewHolder(val binding: ItemMemeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val binding = ItemMemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        val memeUrl = memes[position]

        // Show the progress bar while loading the image
        holder.binding.progressBar.visibility = View.VISIBLE

        // Load the meme image using Glide
        Glide.with(holder.itemView.context)
            .load(memeUrl)
            .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Hide the progress bar when the image load fails
                    holder.binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Hide the progress bar when the image is successfully loaded
                    holder.binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(holder.binding.memeImage)

        // Share button functionality
        holder.binding.shareButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this meme! $memeUrl")
            val chooser = Intent.createChooser(intent, "Share this meme via...")
            holder.itemView.context.startActivity(chooser)
        }

        // Save button functionality
        holder.binding.saveButton.setOnClickListener {
            downloadAndSaveMeme(memeUrl, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int {
        return memes.size
    }

    // Append more memes when loading new data
    fun addMemes(newMemes: List<String>) {
        val startPosition = memes.size
        memes.addAll(newMemes)
        notifyItemRangeInserted(startPosition, newMemes.size)
    }

    // Function to download and save the meme
    private fun downloadAndSaveMeme(imageUrl: String, context: Context) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImageToStorage(resource, context)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle placeholder clearing if necessary
                }
            })
    }

    // Save the bitmap image to the device storage
    private fun saveImageToStorage(bitmap: Bitmap, context: Context) {
        // Get the external storage directory
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        // Ensure the directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Create a unique file name for the saved image
        val fileName = "meme_${System.currentTimeMillis()}.jpg"
        val file = File(storageDir, fileName)

        try {
            // Create an output stream to write the image to the file
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Show a toast to notify the user that the meme was saved
            Toast.makeText(context, "Meme saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save meme", Toast.LENGTH_SHORT).show()
        }
    }
}
