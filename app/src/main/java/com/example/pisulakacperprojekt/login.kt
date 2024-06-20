package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth

    //zmienne
    private lateinit var emailText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerNav: TextView
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
        setContentView(R.layout.activity_login)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()

        emailText = findViewById(R.id.emailText)
        passwordText = findViewById(R.id.passwordText)
        loginButton = findViewById(R.id.loginButton)
        registerNav = findViewById(R.id.registerNav)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        //obsluga nawigacji
        registerNav.setOnClickListener {
            val intent = Intent(applicationContext, register::class.java)
            startActivity(intent)
            finish()
        }

        //obsluga przycisku loginu
        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            //obsluga gdy pusty mail
            if(TextUtils.isEmpty(email)){
                progressBar.visibility = View.GONE
                Toast.makeText(this@login, "Uzupełnij e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //obsluga gdy puste haslo
            if(TextUtils.isEmpty(password)){
                progressBar.visibility = View.GONE
                Toast.makeText(this@login, "Uzupełnij haslo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            //walidacja pola email
            val emailText = emailText.text.toString()
            if (!isValidEmail(emailText)) {
                Toast.makeText(this@login, "Podaj poprawny e-mail", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            //logowanie uzytkownika
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        //val user = auth.currentUser
                        Toast.makeText(this@login, "Pomyslnie zalogowano", Toast.LENGTH_SHORT,).show()

                        //przechodzimy do mainActivity
                        val intent = Intent(applicationContext, mainContent::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@login, "Blad uwierzytelnienia", Toast.LENGTH_SHORT,).show()
                    }
                }
        }
    }
}