package com.gmail.sanyamsoni226.memestation

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MyVolleySingleton private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: MyVolleySingleton? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: MyVolleySingleton(context).also { INSTANCE = it }
        }
    }

    private val requestQueue: RequestQueue by lazy {
        // Use applicationContext to prevent memory leaks
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
