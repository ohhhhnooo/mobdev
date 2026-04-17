package dontdoitno.phonecontactlist

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val contactNames = ArrayList<String>()
    private val contactNumbers = ArrayList<String>()

    // Лаунчер для запроса разрешения на чтение контактов
    // Если разрешение выдано — показываем список, иначе — сообщение об отсутствии разрешения
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showContacts() else showNoPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        savedInstanceState?.let {
            contactNames.addAll(it.getStringArrayList("names") ?: emptyList())
            contactNumbers.addAll(it.getStringArrayList("numbers") ?: emptyList())
            bindList()
        }

        findViewById<Button>(R.id.btnGrantPermission).setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            if (contactNames.isEmpty()) showContacts() else {
                findViewById<ListView>(R.id.lvContacts).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.GONE
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("names", contactNames)
        outState.putStringArrayList("numbers", contactNumbers)
    }

    private fun showContacts() {
        loadContacts()
        bindList()
        findViewById<ListView>(R.id.lvContacts).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.GONE
    }

    private fun showNoPermission() {
        findViewById<ListView>(R.id.lvContacts).visibility = View.GONE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.VISIBLE
    }

    private fun loadContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        cursor?.use { c ->
            while (c.moveToNext()) {
                contactNames.add(c.getString(c.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                contactNumbers.add(c.getString(c.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER)))
            }
        }
    }

    private fun bindList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            contactNames
        )
        findViewById<ListView>(R.id.lvContacts).adapter = adapter
    }
}
