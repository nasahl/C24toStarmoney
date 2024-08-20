package de.nasahl.csv2camt

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

/**
 * Converts C24 transaction downloads in csv format to a file in CAMT format
 */
object Application {

    private var inputFileName: String? = null
    private var propsFileName: String = "properties.txt"

    @JvmStatic
    fun main(args: Array<String>) {
        checkArgs(args)
        val props = PropsReader(File(propsFileName)).read()

        try {
            val inputFile = File(inputFileName)
            require(inputFile.exists()) { "Die Datei $inputFileName existiert nicht!" }
            println("Eingabedatei: $inputFileName")
            println("Konfigurationsdatei: $propsFileName")
            println("SKIP_POCKET_UMBUCHUNG: ${props.skipPocketUmbuchung}")

            val transformer = CsvTransformer(Files.newInputStream(inputFile.toPath()), props.verbose)
            val transactions: List<Transaction> = transformer.convert(props.skipPocketUmbuchung)
            println("Verarbeitete Datensätze: ${transactions.size}")
            require(transactions.isNotEmpty()) { "Es wurden keine Transaktionen gefunden!" }

            val firstDate = transactions.last().date
            val lastDate = transactions.first().date

            val camtFile = File(determineOutputName(firstDate, lastDate))
            println("Ausgabedatei: ${camtFile.name}")

            val content = CamtCreator(props, transactions, LocalDateTime.now()).write()
            camtFile.writeText(content, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun determineOutputName(first: LocalDate, last: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyMMdd")
        return inputFileName!!.lowercase().replace(".csv", "") + "-" + first.format(formatter) + "-" + last.format(formatter) + ".camt053.001.08.xml"
    }

    private fun checkArgs(args: Array<String>) {
        val usage = """
                Usage: csv2camt [-k <konfigurationsdatei>] <invput-csv-file>
                
                Das Programm konvertiert eine aus der C24 Bank Weboberfläche heruntergeladene CSV Datei in ein CAMT Format, 
                um diese dann in Starmoney wieder einzulesen.
                Beim ersten Start wird eine Konfigurationedatei angelegt, die nach den konkreten Bedürfnissen anzupassen sind.
                Die Angabe des Namens einer Konfigurationsdatei ist optional.
                
                Der Workflow ist folgendermassen:
                1. Herunterladen einer CSV Datei aus der C24 Bank Weboberfläche
                2. Ausführen dieses Programms nach Anpassung der Konfigurationsdatei
                3. Öffnen von Starmoney (mein Test lief mit Version 14)
                4. Verwaltung -> Datenimport -> Importdaten: Import
                                                Ziel: Auswählen des Kontos in das importiert wird
                                                Format: Umsätze in camt-Format 053.001.08
                   -> dann mittels <Importieren> auswählen der erzeugten CAMT Datei
        """.trimIndent()

        val arguments = args.toMutableList()
        val index = arguments.indexOf("-k")
        if (index != -1) {
            propsFileName = arguments[index + 1]
            arguments.removeAt(index + 1)
            arguments.removeAt(index)
        }

        if (arguments.isEmpty()) {
            println(usage)
            exitProcess(0)
        }

        inputFileName = arguments[0]
    }
}
