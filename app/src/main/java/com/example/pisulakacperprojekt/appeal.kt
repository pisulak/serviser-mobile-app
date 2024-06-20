package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
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

class appeal : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var reportsCollection: CollectionReference

    //zmienne
    private lateinit var newDescription: EditText
    private lateinit var saveBtn: Button
    private lateinit var backBtn: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appeal)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        reportsCollection = Firebase.firestore.collection("reports")

        newDescription = findViewById(R.id.appealDescription)
        saveBtn = findViewById(R.id.saveAppealBtn)
        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            val intent = Intent(applicationContext, mainContentDone::class.java)
            startActivity(intent)
            finish()
        }

        saveBtn.setOnClickListener {
            appealReport()
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun appealReport() = CoroutineScope(Dispatchers.IO).launch {
        //zmienne podane w intent
        val reportTitle = intent.getStringExtra("reportTitle")

        val query = reportsCollection
            .whereEqualTo("title", reportTitle)
            .get()
            .await()

        if (query.documents.isNotEmpty()) {
            for (document in query) {
                try {
                    val newDesc = newDescription.text.toString()

                    val newData = hashMapOf(
                        "description" to newDesc,
                        "done" to false,
                        "warranty" to true
                    )

                    withContext(Dispatchers.Main) {
                        reportsCollection.document(document.id).set(
                            newData,
                            SetOptions.merge()
                        )
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}