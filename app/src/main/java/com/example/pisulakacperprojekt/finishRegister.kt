package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class finishRegister : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var companiesCollection: CollectionReference

    //zmienne
    private lateinit var usernameText: TextInputEditText
    private lateinit var numberText: TextInputEditText
    private lateinit var locationText: TextInputEditText
    private lateinit var finishButton: Button
    private lateinit var progressBar: ProgressBar

    //walidacja numeru telefonu
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = Regex("^[0-9]{9}\$")
        return phoneRegex.matches(phoneNumber)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_register)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        companiesCollection = Firebase.firestore.collection("companies")

        usernameText = findViewById(R.id.usernameText)
        numberText = findViewById(R.id.numberText)
        locationText = findViewById(R.id.locationText)
        finishButton = findViewById(R.id.finishButton)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        //sprawdzenie czy uzytkownik jest zalogowany
        if(currentUser == null){
            val intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        }

        //obsluga przycisku
        finishButton.setOnClickListener {

            //walidacja numeru telefonu
            val phoneNumber = numberText.text.toString().trim()
            if(phoneNumber.isNotEmpty()){
                if (!isValidPhoneNumber(phoneNumber)) {
                    Toast.makeText(this@finishRegister, "Nieprawidłowy numer telefonu", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if(usernameText.text.toString().isNotEmpty() &&
                currentUser.email.toString().isNotEmpty() &&
                numberText.text.toString().isNotEmpty() &&
                locationText.text.toString().isNotEmpty()) {
                val company = company(
                    usernameText.text.toString(),
                    currentUser.email.toString(),
                    numberText.text.toString(),
                    locationText.text.toString(),
                    false
                )
                progressBar.visibility = View.VISIBLE
                saveCompany(company)
            }
            else {
                Toast.makeText(this@finishRegister, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    private fun saveCompany(company: company) = CoroutineScope(Dispatchers.IO).launch {
        try {
            companiesCollection.add(company).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@finishRegister, "Pomyslnie zapisano dane", Toast.LENGTH_SHORT,).show()
                val intent = Intent(applicationContext, mainContent::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                Toast.makeText(this@finishRegister, "Blad uzupelniania danych", Toast.LENGTH_SHORT,).show()
            }
        }
    }
}