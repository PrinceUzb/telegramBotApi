package uz.scala.telegram.bot.http

import scalaj.http.{Http, MultiPart}
import uz.scala.telegram.bot.api.InputFile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.Try

/**
 * Created by mukel on 8/5/15.
 */
trait ScalajHttpClient extends HttpClient {

  def request(requestUrl: String, params: (String, Any)*): String = {
    // TODO: Set appropriate timeout values
    val query = params.foldLeft(Http(requestUrl)) {
      case (q, (id, value)) => value match {
        case file: InputFile =>
          // TODO: Get the corret MIME type, right now the server ignored it or does some content-based MIME detection
          q.postMulti(MultiPart(id, file.name, file.mimeType, file.bytes))

        case Some(s) =>
          q.param(id, s.toString)

        case None => q

        case _ => q.param(id, value.toString)
      }
    }

    val response = query.asString
    if (response.isSuccess)
      response.body
    else
      throw new Exception("HTTP request error " + response.code + ": " + response.statusLine)
  }

  def asyncRequest(requestUrl: String, params: (String, Any)*): Future[String] = {
    val p = Promise[String]()
    Future {
      p.complete(Try(request(requestUrl, params: _*)))
    }
    p.future
  }
}
