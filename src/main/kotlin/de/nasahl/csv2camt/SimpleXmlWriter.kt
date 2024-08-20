package de.nasahl.csv2camt

class SimpleXmlWriter(tag: String) {

    val root: Elem = Elem(tag)

    open class EBase(val tag: String, var attrKey: String? = null, var attrValue: String? = null)

    data class Elem(private val pTag: String, val values: MutableList<EBase> = mutableListOf()) : EBase(pTag) {
        infix fun add(value: EBase?): Elem {
            value?.let { values.add(it) }
            return this
        }

        fun addAll(valueList: List<EBase?>): Elem {
            valueList.forEach { add(it) }
            return this
        }

        fun addAll(vararg valueList: EBase?): Elem {
            valueList.forEach { add(it) }
            return this
        }

        fun addLeaf(pTag: String, value: Any): Elem {
            add(Leaf(pTag, value))
            return this
        }

        fun attr(attrKey: String, attrValue: String): Elem {
            this.attrKey = attrKey
            this.attrValue = attrValue
            return this
        }
    }

    data class Leaf(private val pTag: String, val value: Any) : EBase(pTag)

    fun write(): String {
        val sb = StringBuilder()
        writeElem(sb, 0, root)
        return sb.toString()
    }

    private fun writeElem(sb: StringBuilder, level: Int, elem: EBase) {
        openTag(sb, level, elem)
        when (elem) {
            is Elem ->
                elem.values.forEach { eBase ->
                    run {
                        when (eBase) {
                            is Leaf -> {
                                writeLeaf(sb, level + 1, eBase)
                            }

                            is Elem -> {
                                writeElem(sb, level + 1, eBase)
                            }
                        }
                    }
                }

            is Leaf -> writeLeaf(sb, level + 1, elem)
        }
        closeTag(sb, level, elem)
    }

    private fun openTag(sb: StringBuilder, level: Int, elem: EBase) {
        if (elem.attrKey == null) {
            sb.appendLine(intend(level) + "<${elem.tag}>")
        } else {
            sb.appendLine(intend(level) + "<${elem.tag} ${elem.attrKey}=\"${elem.attrValue}\">")
        }
    }

    private fun closeTag(sb: StringBuilder, level: Int, elem: EBase) {
        sb.appendLine(intend(level) + "</${elem.tag}>")
    }

    private fun writeLeaf(sb: StringBuilder, level: Int, elem: Leaf) {
        sb.appendLine(intend(level) + "<${elem.tag}>${elem.value}</${elem.tag}>")
    }

    private fun intend(level: Int): String = "   ".repeat(level)
}
