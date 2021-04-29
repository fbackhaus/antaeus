package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider
) {
    private val logger = KotlinLogging.logger {}
    private val maxNumberOfRetries = 3

    fun payPendingInvoices(pendingInvoices: List<Invoice>): MutableList<Invoice> {
        val processedInvoices: MutableList<Invoice> = mutableListOf()

        for (invoice in pendingInvoices) {
            val processedInvoice = payInvoice(invoice)
            processedInvoices.add(processedInvoice)
            //TODO: Use coroutine to schedule a task that runs this process the first day of each month
        }
        return processedInvoices
    }

    private fun payInvoice(invoice: Invoice, retries: Int = 1): Invoice {
        try {
            val successfulCharge = paymentProvider.charge(invoice)
            if (successfulCharge) {
                return markInvoiceAsPaid(invoice)
            }
        } catch (e: NetworkException) {
            logger.warn(e) { "Network issue when trying to pay invoice, retries: $retries" }
            if (shouldRetryPayment(retries)) {
                return payInvoice(invoice, retries + 1)
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception thrown when trying to pay invoice" }
        }
        return invoice
    }

    private fun shouldRetryPayment(retries: Int): Boolean {
        return retries < maxNumberOfRetries
    }

    private fun markInvoiceAsPaid(invoice: Invoice): Invoice {
        return invoice.copy(status = InvoiceStatus.PAID)
    }
}
