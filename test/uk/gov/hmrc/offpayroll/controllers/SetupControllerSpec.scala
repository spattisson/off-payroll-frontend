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

import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.test.{FakeApplication, FakeRequest, RouteInvokers}
import play.api.test.Helpers.{contentAsString, contentType, route, status, _}
import uk.gov.hmrc.offpayroll.WithTestFakeApplication
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.offpayroll.resources._

/**
  * Created by peter on 09/01/2017.
  */
class SetupControllerSpec extends UnitSpec with WithTestFakeApplication with ScalaFutures {

  override def configFile: String = "test-application.conf"


  "GET /setup/" should {
    "return 200 and the first page in Setup" in {
      val result = await(SetupController.apply.begin().apply(FakeRequest("GET", "/setup/")))
      status(result) shouldBe Status.OK

    }
  }

  "GET /start" should {
    "return 200 and the first page Setup" in {
      val result = await(SetupController.apply.begin().apply(FakeRequest("GET", "/start/")))
      status(result) shouldBe Status.OK

    }
  }

  "Submitting the first question to the Setup Controller" should {
    " the second question in the SetupCluster" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        setup_endUserRolePersonDoingWork
      )

      val result = SetupController.apply.processElement(0)(request).futureValue
      status(result) shouldBe Status.OK
      contentAsString(result) should include(setup_hasContractStarted)
    }
  }

}
