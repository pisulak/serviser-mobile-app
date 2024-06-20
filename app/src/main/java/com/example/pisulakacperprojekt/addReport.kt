package com.example.pisulakacperprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class addReport : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var companiesCollection: CollectionReference
    private lateinit var reportsCollection: CollectionReference

    //zmienne
    private lateinit var backBtn: ImageButton
    private lateinit var addReport: Button
    private lateinit var reportTitle: TextInputEditText
    private lateinit var isWarranty: CheckBox
    private lateinit var reportDescription: EditText

    private lateinit var principal: String
    private lateinit var principalEmail: String
    private lateinit var location: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_report)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        companiesCollection = Firebase.firestore.collection("companies")
        reportsCollection = Firebase.firestore.collection("reports")

        backBtn = findViewById(R.id.backButton)
        addReport = findViewById(R.id.addReportButton)
        reportTitle = findViewById(R.id.reportTitle)
        isWarranty = findViewById(R.id.isWarranty)
        reportDescription = findViewById(R.id.reportDescription)

        //sprawdzenie czy uzytkownik jest zalogowany
        if(currentUser == null){
            val intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        }

        //przycisk wstecz
        backBtn.setOnClickListener {
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }

        //obsluga przycisku
        addReport.setOnClickListener {
            if(reportTitle.text.toString().isEmpty()){
                Toast.makeText(this@addReport, "Wpisz tytuł zgłoszenia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(reportDescription.text.toString().isEmpty()){
                Toast.makeText(this@addReport, "Opisz szczegółowo zgłoszenie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            retrieveCompany()
        }
    }

    private fun retrieveCompany() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userEmail = currentUser.email
            if (userEmail != null) {
                val querySnapshot = companiesCollection
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()

                if(querySnapshot.documents.isNotEmpty()) {
                    val company = querySnapshot.documents[0]
                    if (company != null) {
                        withContext(Dispatchers.Main) {
                            principal = company["name"].toString()
                            principalEmail = company["email"].toString()
                            location = company["location"].toString()

                            val isChecked: Boolean = isWarranty.isChecked
                            val report = report(
                                reportTitle.text.toString(),
                                "",
                                location,
                                reportDescription.text.toString(),
                                false,
                                isChecked,
                                "",
                                0,
                                principal,
                                principalEmail
                            )
                            saveReport(report)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Operacje dotyczące interfejsu użytkownika muszą być na wątku głównym
            withContext(Dispatchers.Main) {
                Toast.makeText(this@addReport, "Błąd odtwarzania danych", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveReport(report: report) = CoroutineScope(Dispatchers.IO).launch {
        try {
            reportsCollection.add(report).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@addReport, "Pomyslnie zapisano dane", Toast.LENGTH_SHORT,).show()
                val intent = Intent(applicationContext, mainContent::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                Toast.makeText(this@addReport, "Blad uzupelniania danych", Toast.LENGTH_SHORT,).show()
            }
        }
    }
}