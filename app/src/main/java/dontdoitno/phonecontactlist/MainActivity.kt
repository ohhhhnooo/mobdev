package dontdoitno.phonecontactlist

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showContacts() else showNoPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnGrantPermission).setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            showContacts()
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    private fun showContacts() {
        findViewById<ListView>(R.id.lvContacts).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.GONE
    }

    private fun showNoPermission() {
        findViewById<ListView>(R.id.lvContacts).visibility = View.GONE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.VISIBLE
    }

}