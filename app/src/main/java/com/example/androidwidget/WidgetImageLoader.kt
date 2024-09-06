package com.example.androidwidget
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class WidgetImageLoader(
    private val context: Context,
    private val views: RemoteViews,
    private val appWidgetId: Int,
    private val viewId: Int
) {
    private val client = OkHttpClient()

    fun loadImage(imageUrl: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val bitmap = fetchImage(imageUrl)
            bitmap?.let {
                views.setImageViewBitmap(viewId, it)
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
            }
        }
    }

    private suspend fun fetchImage(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(imageUrl).build()
        try {
            val response = client.newCall(request).execute()
            response.body?.bytes()?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
