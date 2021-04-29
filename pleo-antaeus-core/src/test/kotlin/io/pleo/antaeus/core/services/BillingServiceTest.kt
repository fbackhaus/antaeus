package io.pleo.antaeus.core.services

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.util.getPendingInvoice
import io.pleo.antaeus.core.services.util.getPendingInvoices
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
    }

    private val billingService = BillingService(paymentProvider)

    @Test
    fun `all invoices will be payed correctly`() {
        val pendingInvoices = getPendingInvoices(3)

        val processedInvoices = billingService.payPendingInvoices(pendingInvoices)

        assertEquals(pendingInvoices.size, processedInvoices.size)

        for (i in processedInvoices.indices) {
            assertEquals(pendingInvoices[i].amount, processedInvoices[i].amount)
            assertEquals(pendingInvoices[i].customerId, processedInvoices[i].customerId)
            assertEquals(pendingInvoices[i].id, processedInvoices[i].id)
            assertEquals(InvoiceStatus.PAID, processedInvoices[i].status)
        }
    }

    @Test
    fun `when a CurrencyMismatchException is thrown, there are no retries and the invoice is still pending`() {
        val pendingInvoice = getPendingInvoice()

        every { paymentProvider.charge(pendingInvoice) } throws CurrencyMismatchException(pendingInvoice.id, pendingInvoice.customerId)

        val processedInvoices = billingService.payPendingInvoices(listOf(pendingInvoice))

        verifyNonRetryableExceptionWasThrown(pendingInvoice, processedInvoices)
    }

    @Test
    fun `when a CustomerNotFoundException is thrown, there are no retries and the invoice is still pending`() {
        val pendingInvoice = getPendingInvoice()

        every { paymentProvider.charge(pendingInvoice) } throws CustomerNotFoundException(pendingInvoice.customerId)

        val processedInvoices = billingService.payPendingInvoices(listOf(pendingInvoice))

        verifyNonRetryableExceptionWasThrown(pendingInvoice, processedInvoices)
    }

    @Test
    fun `when a NetworkException is thrown and all the retries are exceeded, the invoice is still pending`() {
        val pendingInvoice = getPendingInvoice()

        every { paymentProvider.charge(pendingInvoice) } throws NetworkException()

        val processedInvoices = billingService.payPendingInvoices(listOf(pendingInvoice))

        verifyRetryableExceptionWasThrownButRetriesExceeded(pendingInvoice, processedInvoices)
    }

    @Test
    fun `when a NetworkException is thrown but the last retry is successful, the invoice is paid successfully`() {
        val pendingInvoice = getPendingInvoice()

        every { paymentProvider.charge(pendingInvoice) } throws NetworkException() andThenThrows NetworkException() andThen true

        val processedInvoices = billingService.payPendingInvoices(listOf(pendingInvoice))

        verifyRetryableExceptionWasThrownItGotRecovered(pendingInvoice, processedInvoices)
    }

    private fun verifyRetryableExceptionWasThrownItGotRecovered(pendingInvoice: Invoice, processedInvoices: MutableList<Invoice>) {
        assertEquals(1, processedInvoices.size)

        assertEquals(pendingInvoice.amount, processedInvoices.first().amount)
        assertEquals(pendingInvoice.customerId, processedInvoices.first().customerId)
        assertEquals(pendingInvoice.id, processedInvoices.first().id)
        assertEquals(InvoiceStatus.PAID, processedInvoices.first().status)

        verify(exactly = 3) { paymentProvider.charge(pendingInvoice) }
        confirmVerified(paymentProvider)
    }

    private fun verifyNonRetryableExceptionWasThrown(pendingInvoice: Invoice, processedInvoices: List<Invoice>) {
        assertEquals(1, processedInvoices.size)

        assertEquals(pendingInvoice.amount, processedInvoices.first().amount)
        assertEquals(pendingInvoice.customerId, processedInvoices.first().customerId)
        assertEquals(pendingInvoice.id, processedInvoices.first().id)
        assertEquals(InvoiceStatus.PENDING, processedInvoices.first().status)

        verify(exactly = 1) { paymentProvider.charge(pendingInvoice) }
        confirmVerified(paymentProvider)
    }

    private fun verifyRetryableExceptionWasThrownButRetriesExceeded(pendingInvoice: Invoice, processedInvoices: List<Invoice>) {
        assertEquals(1, processedInvoices.size)

        assertEquals(pendingInvoice.amount, processedInvoices.first().amount)
        assertEquals(pendingInvoice.customerId, processedInvoices.first().customerId)
        assertEquals(pendingInvoice.id, processedInvoices.first().id)
        assertEquals(InvoiceStatus.PENDING, processedInvoices.first().status)

        verify(exactly = 3) { paymentProvider.charge(pendingInvoice) }
        confirmVerified(paymentProvider)
    }
}
