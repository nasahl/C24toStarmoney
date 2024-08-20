package de.nasahl.csv2camt

import de.nasahl.csv2camt.SimpleXmlWriter.Elem
import de.nasahl.csv2camt.SimpleXmlWriter.Leaf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SimpleXmlWriterTest {

    val expected = """
        <Document>
           <BkToCstmrStmt>
              <ListTag>
                 <letter>A</letter>
                 <letter>B, C</letter>
              </ListTag>
           </BkToCstmrStmt>
           <GrpHdr>
              <MsgId>abc</MsgId>
              <CreDtTm>2024-08-20</CreDtTm>
              <Stmt>
                 <Id>979235204</Id>
              </Stmt>
              <Amt Ccy="EUR">
                 <value>800.0</value>
              </Amt>
           </GrpHdr>
        </Document>
    
    """.trimIndent()

    @Test
    fun testWriting() {
        val writer = SimpleXmlWriter("Document")
        writer.root
            .addAll(
                Elem("BkToCstmrStmt")
                    .add(
                        Elem("ListTag").addAll(listOf("A", "B, C").map { Leaf("letter", it) }),
                    ),
                Elem("GrpHdr")
                    .addLeaf("MsgId", "abc")
                    .addLeaf("CreDtTm", LocalDate.now())
                    .add(
                        Elem("Stmt")
                            .addLeaf("Id", "979235204"),
                    ).add(
                        Elem("Amt")
                            .attr("Ccy", "EUR")
                            .addLeaf("value", 800.0),
                    ),

            )

        val result = writer.write()

        assertThat(result).isEqualTo(expected)
    }
}
