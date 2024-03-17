package com.fatecrl.safehide

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText;
    lateinit var emailInput: EditText;
    lateinit var passwordInput: EditText;
    lateinit var repeatPasswordInput: EditText;
    lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        usernameInput = findViewById(R.id.username_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        repeatPasswordInput = findViewById(R.id.repeatPassword_input)
        btnRegister = findViewById(R.id.register_btn)

        btnRegister.setOnClickListener(){
            val username = usernameInput.text.toString();
            val email = emailInput.text.toString();
            val password = passwordInput.text.toString();
            val repeatPassword = repeatPasswordInput.text.toString();

            Log.i("Test Credentials", "Username: $username | Email: $email | Password: $password | Confirm password: $repeatPassword");
        }
    }
}