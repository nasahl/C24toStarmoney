package de.nasahl.csv2camt

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CsvTransformer(private val inputStream: InputStream, private val verbose: Boolean) {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val fieldMap: MutableMap<String, Int> = mutableMapOf()

    fun convert(skipPocketUmbuchungen: Boolean): List<Transaction> {
        val result = ArrayList<Transaction>()

        inputStream.bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                val parts: List<String> = parseCsvLine(line)
                if (index == 0) {
                    parts.forEachIndexed { index2, part -> fieldMap[part] = index2 }
                } else {
                    val typ = asString("Transaktionstyp", parts)
                    if (!skipPocketUmbuchungen || typ != "Pocket-Umbuchung") {
                        val reason = mutableListOf<String>()
                        asString("Verwendungszweck", parts).takeIf { it.isNotEmpty() }?.let { reason.add(it) }
                        asString("Beschreibung", parts).takeIf { it.isNotEmpty() }?.let { reason.add(it) }
                        if (reason.isEmpty()) reason.add(typ)

                        val creditor = asString("Zahlungsempfänger", parts)
                        val transaction =
                            Transaction(
                                LocalDate.parse(asString("Buchungsdatum", parts), formatter),
                                asString("Betrag", parts).replace(',', '.').toDouble(),
                                creditor,
                                asString("IBAN", parts),
                                reason,
                            )
                        if (verbose) println(transaction)
                        result.add(transaction)
                    }
                }
            }
        }
        return result
    }

    private fun parseCsvLine(line: String): List<String> = CsvReader().readAll(line).first().map { it.trim() }

    private fun asString(fieldName: String, parts: List<String>) = parts[fieldMap[fieldName]!!]
}
