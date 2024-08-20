package de.nasahl.csv2camt

import java.time.LocalDate

data class Transaction(val date: LocalDate, val amount: Double, val receiver: String, val iban: String, val reason: List<String>)
