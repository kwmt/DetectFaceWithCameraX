package net.kwmt27.detectfacewithcamerax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.kwmt27.detectfacewithcamerax.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }
}
