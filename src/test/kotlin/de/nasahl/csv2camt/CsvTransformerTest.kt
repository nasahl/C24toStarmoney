package de.nasahl.csv2camt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CsvTransformerTest {
    @Test
    fun test() {
        CsvTransformerTest::class.java.getResourceAsStream("/TransaktionenFromC24Bank.csv").use { inputStream ->
            val tas: List<Transaction> = CsvTransformer(inputStream, false).convert(true)

            assertThat(tas).hasSize(7)
        }
    }
}
