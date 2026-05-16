package dontdoitno.chatapplication.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import dontdoitno.chatapplication.R
import dontdoitno.chatapplication.databinding.ActivityMainBinding
import dontdoitno.chatapplication.ui.chats.ChatsFragment
import dontdoitno.chatapplication.ui.image.ImageFragment
import dontdoitno.chatapplication.ui.login.LoginActivity
import dontdoitno.chatapplication.ui.messages.MessagesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupBackPressedCallback()

        if (savedInstanceState != null) {
            // On rotation: clear back stack and rebuild from ViewModel state
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            setupFragmentsFromState()
        } else {
            setupInitialFragments()
        }
    }

    private fun isTwoPane(): Boolean = findViewById<View>(R.id.chats_container) != null

    private fun setupInitialFragments() {
        if (isTwoPane()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chats_container, ChatsFragment())
                .commit()
            val selectedChannel = viewModel.selectedChannel.value
            if (selectedChannel != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, MessagesFragment())
                    .commit()
            }
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, ChatsFragment())
                .commit()
        }
    }

    private fun setupFragmentsFromState() {
        val selectedChannel = viewModel.selectedChannel.value
        val imagePath = viewModel.currentImagePath.value

        if (isTwoPane()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chats_container, ChatsFragment())
                .commit()
            when {
                imagePath != null -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MessagesFragment())
                        .addToBackStack(null)
                        .commit()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ImageFragment.newInstance(imagePath))
                        .addToBackStack(null)
                        .commit()
                }
                selectedChannel != null -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MessagesFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        } else {
            when {
                imagePath != null -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ChatsFragment())
                        .commit()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MessagesFragment())
                        .addToBackStack(null)
                        .commit()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ImageFragment.newInstance(imagePath))
                        .addToBackStack(null)
                        .commit()
                }
                selectedChannel != null -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ChatsFragment())
                        .commit()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MessagesFragment())
                        .addToBackStack(null)
                        .commit()
                }
                else -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ChatsFragment())
                        .commit()
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is NavigationEvent.OpenMessages -> openMessages()
                is NavigationEvent.OpenImage -> openImage(event.path)
            }
        }

        viewModel.reloginEvent.observe(this) {
            navigateToLogin()
        }
    }

    private fun openMessages() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, MessagesFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun openImage(path: String) {
        if (isTwoPane()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, ImageFragment.newInstance(path))
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, ImageFragment.newInstance(path))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backStackCount = supportFragmentManager.backStackEntryCount
                if (backStackCount > 0) {
                    val topFragment = supportFragmentManager.findFragmentById(R.id.main_container)
                    if (topFragment is ImageFragment) {
                        viewModel.closeImage()
                    } else if (topFragment is MessagesFragment) {
                        viewModel.deselectChannel()
                    }
                    supportFragmentManager.popBackStack()
                } else {
                    if (isTwoPane()) {
                        // In two-pane landscape with no back stack: finish
                        finish()
                    } else {
                        // In single-pane: the only thing on stack is ChatsFragment, so finish
                        finish()
                    }
                }
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
