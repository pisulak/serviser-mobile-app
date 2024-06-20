package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class editProfile : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var companiesCollection: CollectionReference

    private lateinit var editName: TextInputEditText
    private lateinit var editLocation: TextInputEditText
    private lateinit var editNumber: TextInputEditText
    private lateinit var saveBtn: Button
    private lateinit var backBtn: ImageButton

    //walidacja numeru telefonu
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = Regex("^[0-9]{9}\$")
        return phoneRegex.matches(phoneNumber)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        companiesCollection = Firebase.firestore.collection("companies")

        editName = findViewById(R.id.editNameText)
        editLocation = findViewById(R.id.editLocationText)
        editNumber = findViewById(R.id.editNumberText)
        saveBtn = findViewById(R.id.editBtn)
        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }

        saveBtn.setOnClickListener {

            //walidacja numeru telefonu
            val phoneNumber = editNumber.text.toString().trim()
            if(phoneNumber.isNotEmpty()){
                if (!isValidPhoneNumber(phoneNumber)) {
                    Toast.makeText(this@editProfile, "Nieprawidłowy numer telefonu", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            updateInfo()
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateInfo() = CoroutineScope(Dispatchers.IO).launch {
        val query = companiesCollection
            .whereEqualTo("email", currentUser.email)
            .get()
            .await()

        if (query.documents.isNotEmpty()) {
            for(document in query){
                try {
                    val newName = editName.text.toString()
                    val newLocation = editLocation.text.toString()
                    val newNumber = editNumber.text.toString()

                    val newData = HashMap<String, Any>()
                    if (newName.isNotEmpty()) {
                        newData["name"] = newName
                    }
                    if (newLocation.isNotEmpty()) {
                        newData["location"] = newLocation
                    }
                    if (newNumber.isNotEmpty()) {
                        newData["number"] = newNumber
                    }

                    withContext(Dispatchers.Main) {
                        companiesCollection.document(document.id).set(
                            newData,
                            SetOptions.merge()
                        )
                    }
                } catch(_: Exception) {
                }
            }
        }
    }
}