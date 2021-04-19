package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.random.Random

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

    private fun getPendingInvoices(numberOfInvoices: Int): MutableList<Invoice> {
        val pendingInvoices = mutableListOf<Invoice>()
        val customer = Customer(
            id = 1,
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )

        (1..numberOfInvoices).forEach {
            pendingInvoices.add(Invoice(
                id = it,
                customerId = customer.id,
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                ),
                status = InvoiceStatus.PENDING
            ))
        }

        return pendingInvoices
    }
}