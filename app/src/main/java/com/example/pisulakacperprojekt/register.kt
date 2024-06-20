package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class register : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth

    //zmienne
    private lateinit var emailText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginNav: TextView
    private lateinit var progressBar: ProgressBar


    //sprawdzenie czy uzytkownik jest juz zalogowany
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //przechodzimy do mainActivity
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isValidEmail(email: CharSequence?): Boolean {
        return !email.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()

        emailText = findViewById(R.id.emailText)
        passwordText = findViewById(R.id.passwordText)
        registerButton = findViewById(R.id.registerButton)
        loginNav = findViewById(R.id.loginNav)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        //obsluga nawigacji
        loginNav.setOnClickListener {
            val intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        }

        //obsluga przycisku rejestracji
        registerButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            //obsluga gdy pusty mail
            if(TextUtils.isEmpty(email)){
                Toast.makeText(this@register, "Uzupełnij e-mail", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            //obsluga gdy puste haslo
            if(TextUtils.isEmpty(password)){
                Toast.makeText(this@register, "Uzupełnij haslo", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            //walidacja pola email
            val emailText = emailText.text.toString()
            if (!isValidEmail(emailText)) {
                Toast.makeText(this@register, "Podaj poprawny e-mail", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            //tworzenie uzytkownika
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        //val user = auth.currentUser
                        Toast.makeText(this@register, "Utworzono konto", Toast.LENGTH_SHORT,).show()
                        val intent = Intent(applicationContext, finishRegister::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@register, "Blad uwierzytelnienia", Toast.LENGTH_SHORT,).show()
                    }
                }
        }
    }
}