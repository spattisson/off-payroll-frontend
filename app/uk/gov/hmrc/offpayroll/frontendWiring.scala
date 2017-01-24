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

package uk.gov.hmrc.offpayroll

import play.api.Play
import uk.gov.hmrc.offpayroll.connectors.{DecisionConnector, SessionCacheConnector}
import javax.inject.Singleton
import uk.gov.hmrc.offpayroll.services.{FlowService, IR35FlowService}
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.{HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}


trait ServiceRegistry extends ServicesConfig {
  lazy val decisionConnector: DecisionConnector = new FrontendDecisionConnector
  lazy val flowservice: FlowService = IR35FlowService()
  lazy val sessionCacheConnector: SessionCacheConnector =  new FrontendSessionCacheConnector
}

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with AppName with RunMode {
  override val hooks = NoneRequired
}

object FrontendAuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}

@Singleton
class FrontendDecisionConnector extends DecisionConnector with ServicesConfig {
  val decisionURL: String = baseUrl("off-payroll-decision")
  val serviceURL = "off-payroll-decision/decide"
  val http = WSHttp
}

class FrontendSessionCacheConnector extends SessionCacheConnector with ServicesConfig {
  override val sessionKey: String = getConfString("keystore.sessionKey",
    throw new RuntimeException("Could not find session key"))
  override def defaultSource: String = Play.current.configuration.getString("appName").getOrElse("APP NAME NOT SET")
  override def baseUri: String = baseUrl("keystore")
  override def domain: String = getConfString("keystore.domain",
    throw new RuntimeException("Could not find config keystore.domain"))
  override def http: HttpGet with HttpPut with HttpDelete = WSHttp
}
