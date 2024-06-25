package com.fatecrl.safehide

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class NoTouchableScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notouchable_screen)

        // Make this activity fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Disable all user interaction
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Desabilitar outros botões físicos (como volume, energia, etc.)
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}