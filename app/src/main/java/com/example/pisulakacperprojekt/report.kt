package com.example.pisulakacperprojekt

import com.google.firebase.firestore.PropertyName

data class report (
    var title: String = "",
    var date: String = "",
    var location: String = "",
    var description: String = "",
    var done: Boolean = false,
    var isWarranty: Boolean = false,
    var parts: String = "",
    var cost: Int = 0,
    var principal: String = "",
    var principalEmail: String = ""
) {
    // Konstruktor bezargumentowy
    constructor() : this("", "", "", "", false, false, "", 0, "", "")
}
