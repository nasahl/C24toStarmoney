package de.nasahl.csv2camt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CamtCreatorTest {
    @Test
    fun test() {
        object {}.javaClass.getResourceAsStream("/TransaktionenFromC24Bank-output.camt.xml")?.bufferedReader().use {
            val expectedContent = it?.readText()?.replace("\r\n", "\n")?.replace("\r", "\n")

            object {}.javaClass.getResourceAsStream("/TransaktionenFromC24Bank.csv").use { inputStream ->
                val tas: List<Transaction> = CsvTransformer(inputStream, false).convert(true)

                val camtContent = CamtCreator(Props(), tas.take(2), LocalDateTime.of(2024, 1, 1, 0, 0)).write()
                assertThat(camtContent).isEqualTo(expectedContent)
            }
        }
    }
}
