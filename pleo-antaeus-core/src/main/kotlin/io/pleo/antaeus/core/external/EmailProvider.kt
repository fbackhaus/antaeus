package io.pleo.antaeus.core.external

interface EmailProvider {
    /*
    Sends an email with the provided subject, destination and body

    Returns:
      `True` when the email was successfully sent.
      `False` when the email could not be sent.

    Throws:
      `NetworkException`: when a network error happens.
 */
    fun send(subject: String, to: String, body: Any): Boolean
}
