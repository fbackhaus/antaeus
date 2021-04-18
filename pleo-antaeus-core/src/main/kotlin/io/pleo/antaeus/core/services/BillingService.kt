package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider
) {
    private val logger = KotlinLogging.logger {}

    fun payPendingInvoices(pendingInvoices: List<Invoice>) {
        val processedInvoices: MutableList<Invoice> = mutableListOf()

        for (invoice in pendingInvoices) {
            try {
                paymentProvider.charge(invoice)
                processedInvoices.add(markInvoiceAsPaid(invoice))
            } catch (e: Exception) {
                logger.error(e) { "Exception thrown when trying to pay invoice" }
                processedInvoices.add(invoice)
            }
            //TODO: Retry when there's a NetworkException
            //TODO: Update invoices in the database
            //TODO: Send email/message to notify that the process has run, and what's the outcome
        }
    }

    private fun markInvoiceAsPaid(invoice: Invoice): Invoice {
        return Invoice(
            invoice.id,
            invoice.customerId,
            invoice.amount,
            InvoiceStatus.PAID
        )
    }


}
