package io.pleo.antaeus.core.services.util

import io.pleo.antaeus.models.*
import java.math.BigDecimal
import kotlin.random.Random

fun getPendingInvoices(numberOfInvoices: Int): MutableList<Invoice> {
    val pendingInvoices = mutableListOf<Invoice>()

    (1..numberOfInvoices).forEach {
        pendingInvoices.add(getPendingInvoice())
    }

    return pendingInvoices
}

fun getPendingInvoice(): Invoice {
    val customer = Customer(
        id = Random.nextInt(1, 100),
        currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
    )

    return (Invoice(
        id = Random.nextInt(1, 100),
        customerId = customer.id,
        amount = Money(
            value = BigDecimal(Random.nextDouble(10.0, 500.0)),
            currency = customer.currency
        ),
        status = InvoiceStatus.PENDING
    ))
}
