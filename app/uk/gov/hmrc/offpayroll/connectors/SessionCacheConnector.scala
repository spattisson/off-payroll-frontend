package uk.gov.hmrc.offpayroll.connectors

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.offpayroll.typeDefs.Interview
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by peter on 17/01/2017.
  */
trait SessionCacheConnector extends SessionCache with ServicesConfig {
  val sessionKey: String

  def put(body: Interview)(implicit writes: Writes[Interview], hc: HeaderCarrier) = cache[Interview](sessionKey, body)

  def get(implicit hc: HeaderCarrier, reads: Reads[Interview]) = fetchAndGetEntry[Interview](sessionKey)

}
