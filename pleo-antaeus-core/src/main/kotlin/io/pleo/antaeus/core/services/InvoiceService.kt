/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal, private val billingService: BillingService) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun fetch(status: InvoiceStatus): List<Invoice> {
        return dal.fetchInvoices(status)
    }

    fun payPendingInvoices(): MutableList<Invoice> {
        return billingService.payPendingInvoices(fetch(InvoiceStatus.PENDING))
            .also {
                updateInvoicesStatus(it)
            }
    }

    fun updateInvoicesStatus(invoices: List<Invoice>) {
        dal.updateInvoicesStatus(invoices)
    }
}
