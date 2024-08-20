package de.nasahl.csv2camt

import java.io.File
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

class PropsReader(val file: File) {

    fun read(): Props {
        if (file.exists()) {
            return readPropertiesFile()
        } else {
            createPropertiesFile()
            exitProcess(1)
        }
    }

    private fun readPropertiesFile(): Props {
        val properties = Properties()

        try {
            file.inputStream().use { properties.load(it) }

            val props = Props()

            properties.forEach { key, value ->

                when (key.toString()) {
                    "IBAN" -> props.iban = value.toString()
                    "BIC" -> props.bic = value.toString()
                    "SKIP_POCKET_UMBUCHUNG" -> props.skipPocketUmbuchung = value.toString().toBoolean()
                    "VERBOSE" -> props.verbose = value.toString().toBoolean()
                }
            }
            return props
        } catch (e: IOException) {
            println("Die Datei ${file.name} konnte nicht gelesen werden: ${e.message}")
            exitProcess(1)
        }
    }

    private fun createPropertiesFile() {
        val defaultProps = Props()
        defaultProps.iban = "DE00000000000000000000"
        defaultProps.bic = "PBNKDEFFXXX"

        try {
            file.createNewFile()

            file.appendText("# Die IBAN des Kontos für das der Import durchgeführt wird\n")
            file.appendText("IBAN=${defaultProps.iban}\n")
            file.appendText("# Die BIC des Kontos für das der Import durchgeführt wird\n")
            file.appendText("BIC=${defaultProps.bic}\n\n")
            file.appendText("# Bei SKIP_POCKET_UMBUCHUNG aktiv, werden die Umbuchungen zwischen den Pockets ignoriert\n")
            file.appendText("SKIP_POCKET_UMBUCHUNG=${defaultProps.skipPocketUmbuchung}\n")
            file.appendText("# Bei VERBOSE werden mehr Logausgaben geschrieben\n")
            file.appendText("VERBOSE=${defaultProps.skipPocketUmbuchung}\n")

            println("Es ist noch keine Konfigurationsdatei vorhanden. Deshalb wird die Datei '${file.name}' erstellt.")
            println("Bitte öffnen Sie die Datei und passen Sie die einzelnen Werte an")
        } catch (e: IOException) {
            println("Die Datei ${file.name} konnte nicht erzeugt werden: ${e.message}")
        }
    }
}
