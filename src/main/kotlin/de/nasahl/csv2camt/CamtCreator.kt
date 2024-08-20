@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.nasahl.csv2camt

import de.nasahl.csv2camt.SimpleXmlWriter.Elem
import de.nasahl.csv2camt.SimpleXmlWriter.Leaf
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class CamtCreator(private val props: Props, private val transactions: List<Transaction>, val creationTime: LocalDateTime) {
    fun write(): String {
        val firstDate = transactions.last().date
        val lastDate = transactions.first().date
        val totalBalance: Double = transactions.sumOf { it.amount }

        val hash = transactions.hashCode()

        val writer = SimpleXmlWriter("Document")
        writer.root
            .add(
                Elem("BkToCstmrStmt")
                    .addAll(
                        Elem("GrpHdr")
                            .addLeaf("MsgId", hash)
                            .addLeaf("CreDtTm", LocalDate.now()),
                        Elem("Stmt")
                            .addLeaf("Id", "979235204")
                            .add(
                                Elem("StmtPgntn")
                                    .addLeaf("PgNb", "1")
                                    .addLeaf("LastPgInd", true),
                            ).addLeaf("ElctrncSeqNb", 1)
                            .addLeaf("CreDtTm", creationTime)
                            .add(
                                Elem("Acct")
                                    .add(Elem("Id").addLeaf("IBAN", props.iban))
                                    .addLeaf("Ccy", "EUR")
                                    .add(Elem("Svcr").add(Elem("FinInstnId").addLeaf("BICFI", props.bic))),
                            ).add(createBalanceOpen(firstDate))
                            .add(createBalanceClose(lastDate, totalBalance))
                            .addAll(transactions.map { createTransactionEntry(it) }),
                    ),
            )

        return writer.write()
    }

    private fun createBalanceOpen(firstDate: LocalDate): Elem = Elem("Bal")
        .addAll(
            Elem("Tp").add(Elem("CdOrPrtry").addLeaf("Cd", "OPBD")),
            createAmt(0.0),
            Leaf("CdtDbtInd", "CRDT"),
            Elem("Dt").add(Leaf("Dt", firstDate)),
        )

    private fun createBalanceClose(lastDate: LocalDate, totalBalance: Double): Elem = Elem("Bal")
        .addAll(
            Elem("Tp").add(Elem("CdOrPrtry").addLeaf("Cd", "CLBD")),
            createAmt(totalBalance),
            Leaf("CdtDbtInd", "CRDT"),
            Elem("Dt").addLeaf("Dt", lastDate),
        )

    private fun createTransactionEntry(ta: Transaction): Elem = Elem("Ntry")
        .addAll(
            createAmt(ta.amount.absoluteValue),
            Leaf("CdtDbtInd", if (ta.amount > 0) "CRDT" else "DBIT"),
            Elem("Sts").addLeaf("Cd", "BOOK"),
            Elem("BookgDt").addLeaf("Dt", ta.date),
            Elem("ValDt").addLeaf("Dt", ta.date),
            Elem("BkTxCd"),
            Elem("NtryDtls").add(
                Elem("TxDtls")
                    .addAll(
                        createRltdPties(ta),
                        Elem("RltdAgts"),
                        Elem("RmtInf")
                            .add(
                                Elem("Ustrd")
                                    .addAll(ta.reason.map { Leaf("Ustrd", it) }),
                            ),
                    ),
            ),
        )

    private fun createRltdPties(ta: Transaction): SimpleXmlWriter.EBase? = if (ta.receiver.isNotEmpty()) {
        if (ta.amount > 0) {
            Elem("RltdPties")
                .add(Elem("Dbtr").addLeaf("Nm", ta.receiver))
                .add(Elem("DbtrAcct").add(Elem("Id").addLeaf("IBAN", ta.iban)))
        } else {
            Elem("RltdPties")
                .add(Elem("Cdtr").addLeaf("Nm", ta.receiver))
                .add(Elem("CdtrAcct").add(Elem("Id").addLeaf("IBAN", ta.iban)))
        }
    } else {
        null
    }

    private fun createAmt(amount: Double): Elem = Elem("Amt").attr("Ccy", "EUR").addLeaf("value", amount)
}
