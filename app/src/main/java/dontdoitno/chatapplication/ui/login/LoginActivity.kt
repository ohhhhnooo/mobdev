package dontdoitno.chatapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dontdoitno.chatapplication.R
import dontdoitno.chatapplication.databinding.ActivityLoginBinding
import dontdoitno.chatapplication.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()

        // Check for saved credentials only on first create
        if (savedInstanceState == null) {
            viewModel.checkSavedCredentials()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.etUsername.isEnabled = !isLoading
            binding.etPassword.isEnabled = !isLoading
        }

        viewModel.loginError.observe(this) { error ->
            showErrorDialog(error)
        }

        viewModel.loginSuccessEvent.observe(this) {
            navigateToMain()
        }

        viewModel.skipLoginEvent.observe(this) {
            navigateToMain()
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
