package jp.techacademy.yusuke.autoslideshowapp

import android.app.UiAutomation
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.jar.Manifest
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {


    private val PERMISSION_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    private var mHandler = Handler()

    val mumap = mutableMapOf<Int, Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //画僧の許可
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            getContentsInfo()
        }



        //最初の画像を表示
        var imageNum: Int = 1
        imageView.setImageURI(mumap[imageNum])

        //進むボタンを押した時
        buttonNext.setOnClickListener {
            imageNum++
            if (mumap[imageNum] != null) {
                imageView.setImageURI(mumap[imageNum])
            } else {
                imageNum -= (imageNum-1)
                imageView.setImageURI(mumap[imageNum])
            }
        }

        //戻るボタンを押した時
        buttonBack.setOnClickListener {
            imageNum--
            if (mumap[imageNum] != null) {
                imageView.setImageURI(mumap[imageNum])
            } else {
                imageNum = 0
                do {
                    imageNum++
                } while (mumap[imageNum] != null)
                imageNum--
                imageView.setImageURI(mumap[imageNum])

            }
        }

        //再生・停止ボタンを押した時
        buttonPlay.setOnClickListener {
            if (buttonPlay.text == "再生") {
                buttonPlay.text = "停止"
                buttonNext.isEnabled = false
                buttonBack.isEnabled = false
                mTimer = Timer()

                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            imageNum++
                            imageView.setImageURI(mumap[imageNum])
                        }
                    }
                }, 2000, 2000)
            } else {
                buttonPlay.text = "再生"
                mTimer!!.cancel()
                buttonNext.isEnabled = true
                buttonBack.isEnabled = true
            }


        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {

        var imageId: Int = 1

        //画僧の情報を取得
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor!!.moveToFirst()) {

            do {

                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri: Uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                mumap.put(imageId, imageUri)

                imageId++


            } while (
                cursor.moveToNext())
        }
        cursor.close()
    }
}