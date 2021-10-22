//package com.utils
//
//import com.google.api.client.auth.oauth2.Credential
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
//import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, OAuth2Utils}
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
//import com.google.api.client.http.FileContent
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.{JsonFactory, JsonGenerator}
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.client.util.store.FileDataStoreFactory
//import com.google.api.services.drive.model.File
//import com.google.api.services.drive.{Drive, DriveScopes}
//import play.api.{Configuration, Logger}
//import play.api.libs.ws.WSClient
//
//import java.io.{FileInputStream, InputStream, InputStreamReader}
//import java.util
//import java.util.{Collections, List}
//import javax.inject.{Inject, Singleton}
//import scala.concurrent.Future
//
//@Singleton
//class GDriveClient @Inject()(configuration: Configuration){
//  private val uploadURL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable"
//
//  private val CLIENT_ID = "498281231182-g3r7uvek9u8kp6ioo94im3pr5f3np5l8.apps.googleusercontent.com"
//  private val CLIENT_SECRET = "H9ZHfDJhWa9Q3rE8_oNkuUcM"
//  private val REDIRECT_URI = "https://developers.google.com/oauthplayground"
//
//  private val REFRESH_TOKEN = "1//04OVgtfuAounsCgYIARAAGAQSNwF-L9IrZ8pC1l2tXYAptja3fPh0ziPRobz6ah4Ed7QjAtn-hzxqKCjdWYVzPzUU1ytCU-Kkx5E"
//
//  private val APPLICATION_NAME = "loscars-mp"
//  private val JSON_FACTORY = GsonFactory.getDefaultInstance
////  private val JSON_FACTORY = new JsonGenerator()
//  private val TOKENS_DIRECTORY_PATH = "tokens"
//  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
//
////  private val service: Drive = new Drive.Builder(httpTransport, )
//
//  /**
//   * Global instance of the scopes required by this quickstart.
//   * If modifying these scopes, delete your previously saved tokens/ folder.
//   */
//  private val SCOPES = Collections.singletonList(DriveScopes.DRIVE)
//  private val CREDENTIALS_FILE_PATH = configuration.getString("CREDENTIAL_FILE").getOrElse("client_secret.json")
//  Logger.info(s"Credential file path: $CREDENTIALS_FILE_PATH")
//
//  /**
//   * Creates an authorized Credential object.
//   *
//   * @param HTTP_TRANSPORT The network HTTP Transport.
//   * @return An authorized Credential object.
//   * @throws IOException If the credentials.json file cannot be found.
//   */
//  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport): Option[Credential] = {
////    val in = classOf[GDriveClient].getResourceAsStream(CREDENTIALS_FILE_PATH)
//    val in = new FileInputStream(new java.io.File(CREDENTIALS_FILE_PATH))
//    if (in == null) {
//      Logger.warn("Resource file not found: " + CREDENTIALS_FILE_PATH)
//      None
//    } else {
//      val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
//      val flow = new GoogleAuthorizationCodeFlow.Builder(
//        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//        .setAccessType("online")
//        .build
//      val receiver = new LocalServerReceiver.Builder().setHost("https://developers.google.com/oauthplayground").build
////      val receiver = new LocalServerReceiver.Builder().setPort(8080).build
//
//      Option(new AuthorizationCodeInstalledApp(flow, receiver).authorize("OWNER"))
//    }
//  }
//
//  private val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport
//  private val credential = getCredentials(HTTP_TRANSPORT).get
//
//  val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//    .setApplicationName(APPLICATION_NAME)
//    .build;
//
//  def uploadFile(filePath: String, fileName: String): Boolean = {
//    val fileMetadata = new File
//    fileMetadata.setName(fileName)
//
//    val file = new java.io.File(filePath)
//    val mediaContent = new FileContent("image/png", file)
//    val createdFile = drive.files.create(fileMetadata, mediaContent).setFields("id").execute
//    val id = createdFile.getId
//    Logger.info(s"Created file $fileName with id: $id")
//    id.nonEmpty
//  }
//
//}
