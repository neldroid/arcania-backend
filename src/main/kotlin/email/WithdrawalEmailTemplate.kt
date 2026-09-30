package email

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Emails for a withdrawal ("desistimiento") submitted through the online
 * withdrawal function.
 *
 * Art. 11a of Directive 2011/83/EU (added by Directive (EU) 2023/2673) makes
 * the trader acknowledge receipt, on a durable medium and without undue delay,
 * including the content of the statement and the date and time it was
 * received. The copy to the operator is what starts the refund.
 */
object WithdrawalEmailTemplate {

    data class Statement(
        val name: String,
        val email: String,
        val orderDescription: String,
        val purchaseDate: String,
        val receivedAt: ZonedDateTime,
    )

    private val FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm:ss (zzz)", Locale.forLanguageTag("es-ES"))

    fun receivedNow(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))

    private fun escape(value: String): String = buildString {
        for (char in value) when (char) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(char)
        }
    }

    private fun details(statement: Statement): String = """
        <table role="presentation" cellpadding="6" cellspacing="0" style="font-size:14px; color:#222;">
          <tr><td><strong>Nombre</strong></td><td>${escape(statement.name)}</td></tr>
          <tr><td><strong>Email</strong></td><td>${escape(statement.email)}</td></tr>
          <tr><td><strong>Compra</strong></td><td>${escape(statement.orderDescription)}</td></tr>
          <tr><td><strong>Fecha de compra</strong></td><td>${escape(statement.purchaseDate)}</td></tr>
          <tr><td><strong>Recibido</strong></td><td>${FORMATTER.format(statement.receivedAt)}</td></tr>
        </table>
    """.trimIndent()

    fun acknowledgementSubject(): String = "Hemos recibido tu desistimiento — Magestika"

    fun acknowledgementHtml(statement: Statement): String = """
        <!doctype html>
        <html lang="es"><head><meta charset="utf-8" /></head>
        <body style="font-family: system-ui, -apple-system, 'Segoe UI', sans-serif; line-height:1.6; color:#222; max-width:560px; margin:0 auto; padding:24px 16px;">
          <p>Hola, ${escape(statement.name)}:</p>
          <p>Confirmamos que hemos recibido tu declaración de desistimiento con el siguiente contenido:</p>
          ${details(statement)}
          <p>Te reembolsaremos las compras que no hayas llegado a consumir en un plazo máximo de 14 días
          naturales desde la recepción, por el mismo medio de pago que utilizaste. Las lecturas ya
          generadas no son reembolsables, porque al generarlas solicitaste la prestación inmediata y
          aceptaste perder el derecho de desistimiento sobre ellas.</p>
          <p>Si tienes cualquier duda, responde a este correo o escríbenos a info@ezepiko.com.</p>
          <p style="font-size:12px; color:#666;">Gisela Elizabeth Fernández Lemos (Ezepiko) · NIF 61248326Q ·
          Calle Marquesa de Villa Antonia, 4, piso 4E, 28343 Valdemoro (Madrid), España</p>
        </body></html>
    """.trimIndent()

    fun operatorSubject(statement: Statement): String = "Desistimiento recibido: ${statement.email}"

    fun operatorHtml(statement: Statement): String = """
        <!doctype html>
        <html lang="es"><head><meta charset="utf-8" /></head>
        <body style="font-family: system-ui, sans-serif; line-height:1.6; color:#222;">
          <p>Se ha recibido una declaración de desistimiento a través de la web. Plazo de reembolso:
          14 días naturales desde la recepción.</p>
          ${details(statement)}
        </body></html>
    """.trimIndent()
}
