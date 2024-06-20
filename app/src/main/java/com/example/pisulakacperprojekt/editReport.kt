package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
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

class editReport : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var reportsCollection: CollectionReference

    //zmienne
    private lateinit var editDate: TextInputEditText
    private lateinit var editCost: EditText
    private lateinit var editDone: CheckBox
    private lateinit var editParts: EditText
    private lateinit var editBtn: Button
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_report)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        reportsCollection = Firebase.firestore.collection("reports")

        editDate = findViewById(R.id.editDateText)
        editCost = findViewById(R.id.editCost)
        editDone = findViewById(R.id.editDone)
        editParts = findViewById(R.id.editParts)
        editBtn = findViewById(R.id.editBtn)
        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }

        val reportWarrantyText = intent.getStringExtra("reportWarrantyText")
        if(reportWarrantyText=="Podlega gwarancji"){
            editCost.setText("Nie można dodać ceny przy gwarancji")
            editCost.isEnabled = false
            editCost.isCursorVisible = false
            editCost.inputType = InputType.TYPE_NULL
        }

        editBtn.setOnClickListener {
            editReport()
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun editReport() = CoroutineScope(Dispatchers.IO).launch {
        //zmienne podane w intent
        val reportTitle = intent.getStringExtra("reportTitle")

        val query = reportsCollection
            .whereEqualTo("title", reportTitle)
            .get()
            .await()

        if (query.documents.isNotEmpty()) {
            for(document in query){
                try {
                    val newDate = editDate.text.toString()
                    val newCost = editCost.text.toString().toIntOrNull() ?: 0
                    val newDone = editDone.isChecked
                    val newParts = editParts.text.toString()

                    val newData = hashMapOf(
                        "date" to newDate,
                        "cost" to newCost,
                        "done" to newDone,
                        "parts" to newParts
                    )

                    withContext(Dispatchers.Main) {
                        reportsCollection.document(document.id).set(
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