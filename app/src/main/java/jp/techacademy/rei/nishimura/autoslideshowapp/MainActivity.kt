package jp.techacademy.rei.nishimura.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mCursor: Cursor? = null

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        back_button.setOnClickListener (this)
        start_stop_button.setOnClickListener (this)
        next_button.setOnClickListener (this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    override fun onClick(v: View) {

        if (mCursor == null) {
            Snackbar.make(v, "パーミッションを許可してください", Snackbar.LENGTH_SHORT).show()
            return
        }
        when (v.id) {
            R.id.back_button -> {
                if (mCursor!!.moveToPrevious()) {
                    setImageView()
                } else if (mCursor!!.moveToLast()) {
                    setImageView()
                }
            }

            R.id.start_stop_button -> {
                if (mTimer != null){
                    start_stop_button.text = "再生"
                    back_button.isEnabled = true
                    next_button.isEnabled  = true
                    mTimer!!.cancel()
                    mTimer = null

                } else {
                    start_stop_button.text = "停止"
                    back_button.isEnabled  = false
                    next_button.isEnabled  = false
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                if (mCursor!!.moveToNext()) {
                                    setImageView()

                                } else if (mCursor!!.moveToFirst()) {
                                    setImageView()
                                }
                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒 に設定
                }
            }

            R.id.next_button -> {
                if (mCursor!!.moveToNext()) {
                    setImageView()
                } else if (mCursor!!.moveToFirst()) {
                    setImageView()
                }
            }
        }
     }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        mCursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (mCursor!!.moveToFirst()) {
           setImageView()
        }
    }

    private fun setImageView() {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = mCursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = mCursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        Log.d("ANDROID", "URI : " + imageUri.toString())
        imageView.setImageURI(imageUri)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCursor!!.close()
    }
}