package com.utils

import javax.inject.Inject
import play.api.Configuration

case class CertificateDetails(isProduction: Boolean, poolSize: Int, fileName: String, password: String)

class CertificateHandler @Inject()(config: Configuration) {

  def getCertificate(configPrefix: String): Option[CertificateDetails] = {
    for {
      production <- config.getBoolean( s"${configPrefix}apns.production")
      poolSize <- config.getInt(s"${configPrefix}apns.pool_size")
      fileName <- config.getString(s"${configPrefix}apns.cert_filename")
      filePath <- config.getString("CERT_PATH")
      password <- config.getString(s"${configPrefix}apns.cert_password")
    } yield {
      CertificateDetails(production, poolSize, s"$filePath/$fileName", password)
    }
  }

}