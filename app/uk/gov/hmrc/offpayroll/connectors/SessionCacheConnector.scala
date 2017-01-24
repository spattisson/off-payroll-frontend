/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.offpayroll.connectors

import com.google.inject.ImplementedBy
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.offpayroll.FrontendSessionCacheConnector
import uk.gov.hmrc.offpayroll.models.SessionInterview
import uk.gov.hmrc.offpayroll.modelsFormat._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by peter on 17/01/2017.
  */
@ImplementedBy(classOf[FrontendSessionCacheConnector])
trait SessionCacheConnector extends SessionCache with ServicesConfig {
  val sessionKey: String

  def put(body: SessionInterview)(implicit writes: Writes[SessionInterview], hc: HeaderCarrier) = cache[SessionInterview](sessionKey, body)

  def get(implicit hc: HeaderCarrier, reads: Reads[SessionInterview]) = fetchAndGetEntry[SessionInterview](sessionKey)

}
