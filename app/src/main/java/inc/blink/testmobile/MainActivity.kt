package inc.blink.testmobile

import ImageItem
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(container)

        setupFullScreen()


        loadAndProcessImage()

    }

    private fun setupFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    private fun loadAndProcessImage() {
        val jsonString = loadJSONFromAsset("images.json")

        if (jsonString == null) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Ошибка загрузки изображения из json",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }
        processJsonAndDraw(jsonString)
    }

    private fun loadJSONFromAsset(filename: String): String? {
        return try {
            val inputStream: InputStream = assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {

            try {
                val list = assets.list("")
                Log.d(TAG, "Файл в ассетах: ${list?.joinToString()}")
            } catch (e: Exception) {
                Log.e(TAG, "Нет списка в ассетах", e)
            }
            null
        }
    }

    private fun processJsonAndDraw(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val itemsArray = jsonObject.getJSONArray("items")

            val itemList = mutableListOf<ImageItem>()
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val base64Image = item.getString("image")
                val position = item.getInt("position")
                itemList.add(ImageItem(base64Image, position))
            }

            val n = Math.sqrt(itemList.size.toDouble()).toInt()
            if (n * n != itemList.size) {
                val error = "Номер элемента (${itemList.size})"
                Log.e(TAG, error)
                runOnUiThread {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
                return
            }

            val matrix = Array(n) { arrayOfNulls<ImageItem>(n) }
            for (item in itemList) {
                val pos = item.position
                val row = (pos - 1) / n
                val col = if (row % 2 == 0) {
                    (pos - 1) % n
                } else {
                    n - 1 - ((pos - 1) % n)
                }

                if (row < n && col < n) {
                    matrix[row][col] = item
                }
            }

            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val baseSize = minOf(screenWidth, screenHeight)
            val tileSize = baseSize / n

            val centeredLayout = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val gridLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    tileSize * n,
                    tileSize * n
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }

            for (row in 0 until n) {
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        tileSize
                    )
                }

                for (col in 0 until n) {
                    val imageItem = matrix[row][col]
                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(tileSize, tileSize)
                        scaleType = ImageView.ScaleType.FIT_XY
                    }

                    if (imageItem != null) {
                        try {
                            val decodedBytes = Base64.decode(imageItem.base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            imageView.setBackgroundColor(0xFFFF0000.toInt())
                            imageView.setImageBitmap(null)
                        }
                    } else {
                        imageView.setBackgroundColor(0xFFCCCCCC.toInt())
                    }

                    rowLayout.addView(imageView)
                }

                gridLayout.addView(rowLayout)
            }

            centeredLayout.addView(gridLayout)
            container.addView(centeredLayout)


        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Ошибка с json: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}