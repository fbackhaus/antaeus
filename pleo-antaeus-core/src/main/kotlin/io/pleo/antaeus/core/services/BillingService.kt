package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider
) {

    fun payPendingInvoices(invoices: List<Invoice>) {
        for (invoice in invoices) {
            if (invoice.status === InvoiceStatus.PENDING) {
                paymentProvider.charge(invoice)
                //TODO: How do we change the invoice status to paid?
            }
        }
    }
}
