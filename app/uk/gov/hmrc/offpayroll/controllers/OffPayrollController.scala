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

package uk.gov.hmrc.offpayroll.controllers

import play.Logger
import uk.gov.hmrc.offpayroll.connectors.SessionCacheConnector
import uk.gov.hmrc.offpayroll.services.FragmentService
import uk.gov.hmrc.offpayroll.typeDefs.Interview
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by peter on 11/01/2017.
  */
abstract class OffPayrollController extends FrontendController
  with OffPayrollControllerHelper {

  val fragmentService = FragmentService("/guidance/")
  val sessionCacheConnector: SessionCacheConnector

  protected def updateOrCreateInCache(found: (Interview) => Interview, notFound: () => Interview)
                                     (implicit hc: HeaderCarrier) = {
    sessionCacheConnector.get.flatMap {
      case Some(interview) =>
        Logger.info("Interview found merging new data")
        sessionCacheConnector.put(found(interview))
      case None =>
        Logger.info("No Interview found creating new one")
        sessionCacheConnector.put(notFound())
    }
  }

}
