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

    // Список имён контактов
    private val contactNames = ArrayList<String>()

    // Список номеров телефонов (индекс совпадает с contactNames)
    private val contactNumbers = ArrayList<String>()

    // Лаунчер для запроса разрешения на чтение контактов.
    // Если разрешение выдано — показываем список, иначе — сообщение об отсутствии разрешения.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showContacts() else showNoPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Восстанавливаем списки после поворота экрана, чтобы не перезагружать контакты
        savedInstanceState?.let {
            contactNames.addAll(it.getStringArrayList("names") ?: emptyList())
            contactNumbers.addAll(it.getStringArrayList("numbers") ?: emptyList())
            bindList()
        }

        // Кнопка для повторного запроса разрешения, если оно было отклонено
        findViewById<Button>(R.id.btnGrantPermission).setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }

        // Проверяем, есть ли уже разрешение на чтение контактов
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            // Если список уже загружен (после поворота) — просто показываем его
            // Если пустой — загружаем контакты
            if (contactNames.isEmpty()) showContacts() else {
                findViewById<ListView>(R.id.lvContacts).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.GONE
            }
        } else {
            // Разрешения нет — запрашиваем у пользователя
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    // Сохраняем списки контактов перед уничтожением Activity (поворот экрана)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("names", contactNames)
        outState.putStringArrayList("numbers", contactNumbers)
    }

    // Загружает контакты из базы данных и показывает список
    private fun showContacts() {
        loadContacts()
        bindList()
        findViewById<ListView>(R.id.lvContacts).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.GONE
    }

    // Скрывает список и показывает сообщение об отсутствии разрешения
    private fun showNoPermission() {
        findViewById<ListView>(R.id.lvContacts).visibility = View.GONE
        findViewById<LinearLayout>(R.id.layoutNoPermission).visibility = View.VISIBLE
    }

    // Читает контакты с телефона через ContentResolver и заполняет списки имён и номеров
    private fun loadContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME // сортировка по имени
        )

        // Перебираем все строки курсора и добавляем имена и номера в списки
        cursor?.use { c ->
            while (c.moveToNext()) {
                contactNames.add(c.getString(c.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                contactNumbers.add(c.getString(c.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER)))
            }
        }
    }

    // Привязывает список имён к ListView через ArrayAdapter
    private fun bindList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            contactNames
        )
        findViewById<ListView>(R.id.lvContacts).adapter = adapter
    }
}
