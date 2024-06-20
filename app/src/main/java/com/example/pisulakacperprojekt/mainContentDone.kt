package com.example.pisulakacperprojekt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

class mainContentDone : AppCompatActivity() {

    //baza
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var companiesCollection: CollectionReference
    private lateinit var reportsCollection: CollectionReference

    private lateinit var dropdownMenu: Spinner
    private lateinit var addReportBtn: FloatingActionButton
    private lateinit var reportsViewList: LinearLayout
    private lateinit var switchViewBtn: Button
    private var isTechnician: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_content_done)

        //inicjalizacja zmiennych
        auth=FirebaseAuth.getInstance()
        currentUser= auth.currentUser!!
        companiesCollection = Firebase.firestore.collection("companies")
        reportsCollection = Firebase.firestore.collection("reports")

        dropdownMenu = findViewById(R.id.spinnerD)
        addReportBtn = findViewById(R.id.addReportD)
        switchViewBtn = findViewById(R.id.switchViewBtnD)
        reportsViewList = findViewById(R.id.reportsViewListD)
        addReportBtn.visibility = View.INVISIBLE

        checkForTechAndCreateLayout()

        //obsluga przycisku zmiany widoku
        switchViewBtn.setOnClickListener {
            val intent = Intent(applicationContext, mainContent::class.java)
            startActivity(intent)
            finish()
        }

        dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position) as String

                when (selectedOption) {
                    "Edytuj profil" -> {
                        dropdownMenu.visibility = View.INVISIBLE
                        val intent = Intent(applicationContext, editProfile::class.java)
                        startActivity(intent)
                        finish()
                    }
                    "Wyloguj się" -> {
                        dropdownMenu.visibility = View.INVISIBLE
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(applicationContext, login::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //gdy nie wybrano żadnego elementu
            }
        }
    }

    private fun checkForTechAndCreateLayout() = CoroutineScope(Dispatchers.IO).launch {
        val userEmail = currentUser.email
        if (userEmail != null) {
            //zapytanie sprawdzajace tylko konto z zalogowanym emailem
            val querySnapshot = companiesCollection
                .whereEqualTo("email", userEmail)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val company = querySnapshot.documents[0]
                if (company != null) {
                    isTechnician = company["technician"] as? Boolean ?: false

                    withContext(Dispatchers.Main) {
                        if (isTechnician==false) {
                            //jest zalogowany klient
                            //zapytanie odwolujace sie do raportow tylko klienta ktory jest aktualnie zalogowany oraz zgloszen nie zakonczonych
                            val query = reportsCollection
                                .whereEqualTo("principalEmail", userEmail)
                                .whereEqualTo("done", true)
                                .get()
                                .await()

                            try {
                                for (document in query.documents){
                                    val report = document.toObject<report>()
                                    if (report != null) {
                                        //wartosci potrzebne do wyswietlenia raportu
                                        val reportTitle = report.title
                                        var reportWarrantyText = " "
                                        val reportDescription = "Opis: " + report.description
                                        val reportLocation = "Lokalizacja firmy: " + report.location
                                        val reportDate = "Data wykonania: " + report.date
                                        val reportPrice = "Cena: " + report.cost
                                        val reportParts = "Wykorzystane części: " + report.parts
                                        val reportDoneText = "Zakończone"

                                        if (report.isWarranty == true) {
                                            reportWarrantyText = "Podlega gwarancji"
                                        } else {
                                            reportWarrantyText = "Nie podlega gwarancji"
                                        }

                                        //pobranie layoutu z pliku report_layout.xml
                                        val itemLayout = layoutInflater.inflate(R.layout.report_layout_done, reportsViewList, false) as LinearLayout

                                        //zmienne dla small layout z pliku report_layout.xml
                                        val smallReportPrincipal: TextView = itemLayout.findViewById(R.id.smallReportPrincipalD)
                                        val smallReportTitle: TextView = itemLayout.findViewById(R.id.smallReportTitleD)
                                        val smallReportWarrantyText: TextView = itemLayout.findViewById(R.id.smallReportWarrantyTextD)

                                        smallReportPrincipal.visibility = View.GONE
                                        smallReportTitle.text = reportTitle
                                        smallReportWarrantyText.text = reportWarrantyText

                                        //zmienne dla big layout z pliku report_layout.xml
                                        val bigReportPrincipal: TextView = itemLayout.findViewById(R.id.bigReportPrincipalD)
                                        val bigReportTitle: TextView = itemLayout.findViewById(R.id.bigReportTitleD)
                                        val bigReportWarrantyText: TextView = itemLayout.findViewById(R.id.bigReportWarrantyTextD)
                                        val bigReportDescription: TextView = itemLayout.findViewById(R.id.bigReportDescriptionD)
                                        val bigReportLocation: TextView = itemLayout.findViewById(R.id.bigReportLocationD)
                                        val bigReportDate: TextView = itemLayout.findViewById(R.id.bigReportDateD)
                                        val bigReportPrice: TextView = itemLayout.findViewById(R.id.bigReportPriceD)
                                        val bigReportParts: TextView = itemLayout.findViewById(R.id.bigReportPartsD)
                                        val bigReportDone: TextView = itemLayout.findViewById(R.id.bigReportDoneD)
                                        val appealBtn: Button = itemLayout.findViewById(R.id.appealBtn)

                                        //obsluga przycisku odwolania
                                        appealBtn.setOnClickListener {
                                            val intent = Intent(applicationContext, appeal::class.java)
                                            intent.putExtra("reportTitle", reportTitle)
                                            startActivity(intent)
                                            finish()
                                        }

                                        bigReportPrincipal.visibility = View.GONE
                                        bigReportTitle.text = reportTitle
                                        bigReportWarrantyText.text = reportWarrantyText
                                        bigReportDescription.text = reportDescription
                                        bigReportLocation.text = reportLocation
                                        bigReportDate.text = reportDate
                                        bigReportPrice.text = reportPrice
                                        bigReportParts.text = reportParts
                                        bigReportDone.text = reportDoneText

                                        //dynamiczne dodanie raportu dla kazdego elementu w petli
                                        reportsViewList.addView(itemLayout)

                                        //zmienne dla flippera widoku
                                        val reportFlipper = itemLayout.findViewById<ViewFlipper>(R.id.reportFlipperD)
                                        val smallReportContainer = itemLayout.findViewById<LinearLayout>(R.id.smallReportContainerD)
                                        val bigReportContainer = itemLayout.findViewById<LinearLayout>(R.id.bigReportContainerD)

                                        bigReportContainer.visibility = View.GONE
                                        reportFlipper.layoutParams.height = 80 //200 na emulatorze
                                        reportFlipper.requestLayout()

                                        //funkcje do przelaczania widoku
                                        smallReportContainer.setOnClickListener {
                                            reportFlipper.showNext()
                                            bigReportContainer.visibility = View.VISIBLE
                                            reportFlipper.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                                            reportFlipper.requestLayout()
                                        }
                                        bigReportContainer.setOnClickListener {
                                            reportFlipper.showPrevious()
                                            bigReportContainer.visibility = View.GONE
                                            reportFlipper.layoutParams.height = 80 //200 na emulatorze
                                            reportFlipper.requestLayout()
                                        }
                                        reportFlipper.requestLayout()
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                        else {
                            //jest zalogowany serwisant
                            addReportBtn.visibility = View.INVISIBLE

                            //zapytanie odwolujace sie do nie zakonczonych zgloszen
                            val query = reportsCollection
                                .whereEqualTo("done", true)
                                .get()
                                .await()

                            try {
                                for (document in query.documents){
                                    val report = document.toObject<report>()
                                    if (report != null) {
                                        //wartosci potrzebne do wyswietlenia raportu
                                        val reportPrincipal = report.principal
                                        val reportTitle = report.title
                                        var reportWarrantyText = " "
                                        val reportDescription = "Opis: " + report.description
                                        val reportLocation = "Lokalizacja firmy: " + report.location
                                        val reportDate = "Data wykonania: " + report.date
                                        val reportPrice = "Cena: " + report.cost
                                        val reportParts = "Wykorzystane części: " + report.parts
                                        val reportDoneText = "Zakończone"

                                        if (report.isWarranty == true) {
                                            reportWarrantyText = "Podlega gwarancji"
                                        } else {
                                            reportWarrantyText = "Nie podlega gwarancji"
                                        }

                                        //pobranie layoutu z pliku report_layout.xml
                                        val itemLayout = layoutInflater.inflate(R.layout.report_layout_done, reportsViewList, false) as LinearLayout

                                        //zmienne dla small layout z pliku report_layout.xml
                                        val smallReportPrincipal: TextView = itemLayout.findViewById(R.id.smallReportPrincipalD)
                                        val smallReportTitle: TextView = itemLayout.findViewById(R.id.smallReportTitleD)
                                        val smallReportWarrantyText: TextView = itemLayout.findViewById(R.id.smallReportWarrantyTextD)

                                        smallReportPrincipal.text = reportPrincipal
                                        smallReportTitle.text = reportTitle
                                        smallReportWarrantyText.text = reportWarrantyText

                                        //zmienne dla big layout z pliku report_layout.xml
                                        val bigReportPrincipal: TextView = itemLayout.findViewById(R.id.bigReportPrincipalD)
                                        val bigReportTitle: TextView = itemLayout.findViewById(R.id.bigReportTitleD)
                                        val bigReportWarrantyText: TextView = itemLayout.findViewById(R.id.bigReportWarrantyTextD)
                                        val bigReportDescription: TextView = itemLayout.findViewById(R.id.bigReportDescriptionD)
                                        val bigReportLocation: TextView = itemLayout.findViewById(R.id.bigReportLocationD)
                                        val bigReportDate: TextView = itemLayout.findViewById(R.id.bigReportDateD)
                                        val bigReportPrice: TextView = itemLayout.findViewById(R.id.bigReportPriceD)
                                        val bigReportParts: TextView = itemLayout.findViewById(R.id.bigReportPartsD)
                                        val bigReportDone: TextView = itemLayout.findViewById(R.id.bigReportDoneD)
                                        val appealBtn: Button = itemLayout.findViewById(R.id.appealBtn)
                                        appealBtn.visibility = View.GONE

                                        bigReportPrincipal.text = reportPrincipal
                                        bigReportTitle.text = reportTitle
                                        bigReportWarrantyText.text = reportWarrantyText
                                        bigReportDescription.text = reportDescription
                                        bigReportLocation.text = reportLocation
                                        bigReportDate.text = reportDate
                                        bigReportPrice.text = reportPrice
                                        bigReportParts.text = reportParts
                                        bigReportDone.text = reportDoneText

                                        //dynamiczne dodanie raportu dla kazdego elementu w petli
                                        reportsViewList.addView(itemLayout)

                                        //zmienne dla flippera widoku
                                        val reportFlipper = itemLayout.findViewById<ViewFlipper>(R.id.reportFlipperD)
                                        val smallReportContainer = itemLayout.findViewById<LinearLayout>(R.id.smallReportContainerD)
                                        val bigReportContainer = itemLayout.findViewById<LinearLayout>(R.id.bigReportContainerD)

                                        bigReportContainer.visibility = View.GONE
                                        reportFlipper.layoutParams.height = 110 //200 na emulatorze
                                        reportFlipper.requestLayout()

                                        //funkcje do przelaczania widoku
                                        smallReportContainer.setOnClickListener {
                                            reportFlipper.showNext()
                                            bigReportContainer.visibility = View.VISIBLE
                                            reportFlipper.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                                            reportFlipper.requestLayout()
                                        }
                                        bigReportContainer.setOnClickListener {
                                            reportFlipper.showPrevious()
                                            bigReportContainer.visibility = View.GONE
                                            reportFlipper.layoutParams.height = 110 //200 na emulatorze
                                            reportFlipper.requestLayout()
                                        }
                                        reportFlipper.requestLayout()
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }
}