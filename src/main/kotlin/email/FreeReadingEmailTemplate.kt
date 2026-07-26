package email

import domain.tarot.ReadingDefinition

/**
 * HTML for the "here's a free reading" campaign email. Mirrors the frontend's
 * dark/gold "Arcania" palette (see esoterico-frontend/app/globals.css) with
 * inline styles and an email-safe font fallback stack, since email clients
 * don't load CSS custom properties, external stylesheets, or web fonts.
 */
object FreeReadingEmailTemplate {

    fun subject(reading: ReadingDefinition): String =
        "Un regalo para ti: ${reading.displayName}, gratis ✨"

    fun html(userName: String, reading: ReadingDefinition, ctaUrl: String): String {
        val greeting = if (userName.isNotBlank()) "Hola, $userName." else "Hola."

        return """
            <!doctype html>
            <html lang="es">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>${subject(reading)}</title>
            </head>
            <body style="margin:0; padding:0; background-color:#0a0510; font-family: system-ui, -apple-system, 'Segoe UI', sans-serif;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#0a0510; padding: 40px 16px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width:560px; background: linear-gradient(180deg, #1d1730, #15101f); border: 1px solid rgba(212,165,116,0.35); border-radius: 20px; overflow:hidden;">
                      <tr>
                        <td style="padding: 40px 36px 8px 36px; text-align:center;">
                          <p style="margin:0 0 18px 0; font-size:11px; font-weight:600; letter-spacing:0.14em; text-transform:uppercase; color:#d4a574;">
                            MAGESTIKA &middot; REGALO PARA TI
                          </p>
                          <h1 style="margin:0 0 18px 0; font-family: Georgia, 'Times New Roman', serif; font-style:italic; font-weight:500; font-size:28px; line-height:1.25; color:#f4eef9;">
                            Una lectura te está esperando
                          </h1>
                          <p style="margin:0; font-size:14px; line-height:1.8; color:#b9aecf;">
                            $greeting Te regalamos una lectura de <strong style="color:#f4eef9;">${reading.displayName}</strong>,
                            completamente gratis. Sin costo, sin compromiso — solo tú, las cartas, y lo que necesités ver hoy.
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 28px 36px 4px 36px;">
                          <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="height:1px; background-color: rgba(212,165,116,0.35); font-size:0; line-height:0;">&nbsp;</td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 24px 36px 44px 36px; text-align:center;">
                          <a href="$ctaUrl"
                             style="display:inline-block; padding: 14px 32px; border-radius: 999px; background-color:#a855f7; border:1px solid rgba(255,255,255,0.12); color:#ffffff; font-size:15px; font-weight:600; text-decoration:none;">
                            Hacer mi lectura gratis →
                          </a>
                          <p style="margin: 20px 0 0 0; font-size:12px; color:#7a6f95;">
                            Tu lectura ya está acreditada en tu cuenta — solo tenés que entrar y usarla.
                          </p>
                        </td>
                      </tr>
                    </table>
                    <p style="max-width:560px; margin: 24px 0 0 0; font-size:11px; line-height:1.6; color:#7a6f95;">
                      Recibiste este correo porque tenés una cuenta en Magesatika.
                    </p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
    }
}
