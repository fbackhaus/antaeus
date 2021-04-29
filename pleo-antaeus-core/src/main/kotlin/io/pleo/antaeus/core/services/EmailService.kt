package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.EmailProvider
import io.pleo.antaeus.models.Invoice

class EmailService(
    private val emailProvider: EmailProvider
) {

    //These values should be in a config file - not hardcoded
    private val subject = "Billing Monthly Process Results"
    private val to = "billing@pleo.com"

    fun sendEmail(invoices: List<Invoice>) {
        emailProvider.send(subject, to, invoices)
    }
}