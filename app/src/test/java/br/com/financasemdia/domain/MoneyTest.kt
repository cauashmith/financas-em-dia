package br.com.financasemdia.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `aceita formatos brasileiros e com ponto decimal`() {
        assertEquals(123_456L, Money.parseToCents("1.234,56"))
        assertEquals(123_456L, Money.parseToCents("1234,56"))
        assertEquals(123_456L, Money.parseToCents("1234.56"))
        assertEquals(1_000L, Money.parseToCents("R$ 10"))
        assertEquals(1_050L, Money.parseToCents("10,5"))
    }

    @Test
    fun `rejeita entradas invalidas`() {
        assertNull(Money.parseToCents(""))
        assertNull(Money.parseToCents("abc"))
        assertNull(Money.parseToCents("-10"))
        assertNull(Money.parseToCents("10,999"))
        assertNull(Money.parseToCents("1,2,3"))
    }

    @Test
    fun `converte centavos para o texto do campo`() {
        assertEquals("1234,50", Money.toInputText(123_450))
        assertEquals("0,99", Money.toInputText(99))
    }

    @Test
    fun `formata em reais`() {
        assertEquals("R$ 1.234,56", Money.format(123_456))
    }
}
