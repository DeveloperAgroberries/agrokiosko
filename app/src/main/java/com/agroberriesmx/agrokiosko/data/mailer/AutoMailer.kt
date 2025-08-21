package com.agroberriesmx.agrokiosko.data.mailer

import android.content.Context
import com.agroberriesmx.agrokiosko.data.logger.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.*

class AutoMailer(context: Context) {
    private val logUtil: LogUtil = LogUtil(context)

    fun sendEmail(toEmail: String, subject: String, body: String) {
        if(toEmail != " ") {
            val host = "smtp.gmail.com"
            val port = "587"
            val fromEmail = "portal.agroberries@gmail.com"
            val password = "esno yahh evsf mazo"

            //Propiedades del servidor
            val properties = Properties().apply {
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            //Obtener sesion de correo
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication{
                    return PasswordAuthentication(fromEmail, password)
                }
            })

            try {
                //Crear el mensaje
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(fromEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                    this.subject = subject
                    setText(body)
                }

                //Enviar el correo
                Transport.send(message)
                logUtil.logMessage("Se envio un correo electronico a: $toEmail")
            } catch (e: Exception) {
                logUtil.logErrorMessage("Al enviar correo: ${e.message}")
                e.printStackTrace()
            }
        } else {
            logUtil.logErrorMessage("El usuario no cuenta con un correo electronico")
        }
    }

    fun sendEmailInBackground(toEmail: String, subject: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            sendEmail(toEmail, subject, body)
        }
    }
}