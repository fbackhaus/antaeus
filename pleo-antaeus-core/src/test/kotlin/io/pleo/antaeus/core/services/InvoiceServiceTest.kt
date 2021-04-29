package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.services.util.getPendingInvoices
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { updateInvoicesStatus(any()) } returns Unit
    }

    private val billingService = mockk<BillingService> {}

    private val emailService = mockk<EmailService> {
        every { sendEmail(any()) } returns Unit
    }

    private val invoiceService = InvoiceService(dal, billingService, emailService)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `pay pending invoices`() {

        val pendingInvoices = getPendingInvoices(5)
        every { dal.fetchInvoices(InvoiceStatus.PENDING) } returns pendingInvoices

        val payedInvoices = paySomeInvoices(pendingInvoices)

        every { billingService.payPendingInvoices(pendingInvoices) } returns payedInvoices

        val processedInvoices = invoiceService.payPendingInvoices()

        verify(exactly = 1) { dal.updateInvoicesStatus(payedInvoices) }

        verify(exactly = 1) { emailService.sendEmail(payedInvoices) }

        assertEquals(processedInvoices, payedInvoices)
    }

    private fun paySomeInvoices(invoices: MutableList<Invoice>): MutableList<Invoice> {
        for (i in 0 until invoices.size) {
            val invoice = invoices[i]
            if (invoice.id % 2 == 1) {
                invoices[i] = invoice.copy(status = InvoiceStatus.PAID)
            }
        }
        return invoices
    }
}
