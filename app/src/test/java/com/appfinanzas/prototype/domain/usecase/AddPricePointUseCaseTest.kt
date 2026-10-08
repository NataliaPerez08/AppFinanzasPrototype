package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddPricePointUseCaseTest {

    @Test
    fun `saves a valid price with the parsed date`() = runTest {
        val repository = TestInvestmentRepository()

        val result = AddPricePointUseCase(repository).execute(1L, "123.45", "01/02/2025")

        assertTrue(result is AddPricePointResult.Success)
        val history = repository.getPriceHistory(1L)
        assertEquals(1, history.size)
        assertEquals(123.45, history[0].price, 0.001)
        assertEquals(LocalDate.of(2025, 2, 1), history[0].date)
    }

    @Test
    fun `defaults to today when the date is blank`() = runTest {
        val repository = TestInvestmentRepository()

        AddPricePointUseCase(repository).execute(1L, "50", "")

        assertEquals(LocalDate.now(), repository.getPriceHistory(1L).single().date)
    }

    @Test
    fun `rejects a non positive price`() = runTest {
        val repository = TestInvestmentRepository()

        val result = AddPricePointUseCase(repository).execute(1L, "0", "01/02/2025")

        assertTrue(result is AddPricePointResult.Invalid)
        assertTrue(repository.getPriceHistory(1L).isEmpty())
    }

    @Test
    fun `rejects a future date`() = runTest {
        val repository = TestInvestmentRepository()

        val result = AddPricePointUseCase(repository).execute(1L, "100", "01/01/2999")

        assertTrue(result is AddPricePointResult.Invalid)
    }
}
