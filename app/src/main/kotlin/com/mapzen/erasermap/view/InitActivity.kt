package com.mapzen.erasermap.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.SimpleCrypt
import com.mapzen.erasermap.model.ApiKeys
import javax.inject.Inject

public class InitActivity : AppCompatActivity() {
    companion object {
        @JvmStatic public val START_DELAY_IN_MS: Long = 1200
    }

    var crashReportService: CrashReportService? = null
        @Inject set
    var keys: ApiKeys? = null
        @Inject set

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as EraserMapApplication
        app.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.splash_screen)
        supportActionBar.hide()

        if (BuildConfig.DEBUG) {
            (findViewById(R.id.build_number) as TextView).text = BuildConfig.BUILD_NUMBER
        }

        Handler().postDelayed({ startMainActivity() }, START_DELAY_IN_MS)
    }

    private fun startMainActivity() {
        if (BuildConfig.VECTOR_TILE_API_KEY == null ||
                BuildConfig.PELIAS_API_KEY == null ||
                BuildConfig.VALHALLA_API_KEY == null) {
            showApiKeyDialog()
        } else {
            if (BuildConfig.DEBUG) {
                keys?.tilesKey = BuildConfig.VECTOR_TILE_API_KEY
                keys?.searchKey = BuildConfig.PELIAS_API_KEY
                keys?.routingKey = BuildConfig.VALHALLA_API_KEY
            } else {
                val crypt = SimpleCrypt(application)
                keys?.tilesKey = crypt.decode(BuildConfig.VECTOR_TILE_API_KEY)
                keys?.searchKey = crypt.decode(BuildConfig.PELIAS_API_KEY)
                keys?.routingKey = crypt.decode(BuildConfig.VALHALLA_API_KEY)
            }

            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun initCrashReportService() {
        crashReportService?.initAndStartSession(this)
    }

    private fun showApiKeyDialog() {
        var message = "The following API keys are not set: "
        if (BuildConfig.VECTOR_TILE_API_KEY == null) {
            message += "\n* VECTOR TILE"
        }
        if (BuildConfig.PELIAS_API_KEY == null) {
            message += "\n* PELIAS"
        }
        if (BuildConfig.VALHALLA_API_KEY == null) {
            message += "\n* VALHALLA"
        }

        AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Exit Application", { dialogInterface, i -> finish() })
                .show()
    }
}
