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
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.offpayroll.connectors.{SessionCacheConnector, SessionCacheHelper}
import uk.gov.hmrc.offpayroll.resources._
import uk.gov.hmrc.offpayroll.services.{IR35FlowService, InterviewEvaluation, SessionHelper}
import uk.gov.hmrc.offpayroll.{FrontendDecisionConnector, WithTestFakeApplication}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future



class InterviewControllerSpec extends UnitSpec with MockitoSugar with WithTestFakeApplication with ScalaFutures {

  override def configFile: String = "test-application.conf"

  val TEST_SESSION_ID = "41c1fc6444bb7e"

  class TestSessionHelper extends SessionHelper {
    override def createCorrelationId(request: Request[_]): String = TEST_SESSION_ID
  }

  class InstrumentedIR35FlowService extends IR35FlowService(new FrontendDecisionConnector) {
    var passedCorrelationId = ""
    override def evaluateInterview(interview: Map[String, String], currentQnA: (String, String), correlationId: String): Future[InterviewEvaluation] = {
      val futureInterviewEvaluation = super.evaluateInterview(interview, currentQnA, correlationId)
      passedCorrelationId = correlationId
      futureInterviewEvaluation
    }
  }

  object TestSessionCacheConnector extends SessionCacheConnector {
    override val sessionKey: String = "sessionId"
    override def defaultSource: String = "test"
    override def baseUri: String = "http://localhost"
    override def domain: String = "ofpayroll"
    override def http: HttpGet with HttpPut with HttpDelete  = mock[WSHttp]
  }

  object SessionCacheHelper {
    def apply() = new SessionCacheHelper(TestSessionCacheConnector)
  }

  "GET /cluster/" should {
    "return 200" in {
      val result = await(InterviewController().begin.apply(FakeRequest("GET", "/cluster/")))
      status(result) shouldBe Status.OK
    }
  }

  val interview1 = Map(setup_endUserRolePersonDoingWork, setup_hasContractStartedNo, setupIntermediary,
    officeHolderNo, exit_conditionsLiabilityIndvidualIntermediaryYes).toSeq

  "POST /cluster/0/element/0" should {
    "return 200" in {

//      implicit val request = FakeRequest().withFormUrlEncodedBody(
//        personalService_contractualObligationForSubstituteYes
//      ).withSession(interview1 :_*)

      implicit val request = FakeRequest().withFormUrlEncodedBody(
        personalService_contractualObligationForSubstituteYes
      ).withSession(interview1 :_*)

      val result = new InterviewController(IR35FlowService(),
        new TestSessionHelper(), SessionCacheHelper()).processElement(0, 0)(request).futureValue
      status(result) shouldBe Status.OK
      contentAsString(result) should include(personalService_contractualObligationInPractise)
    }
  }

  "POST /cluster/0/element/0 without a cookie" should {
    "intercept an exception" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        personalService_contractualObligationForSubstituteYes
      )
      intercept[NoSuchElementException]{InterviewController().processElement(0, 0)(request).futureValue}
    }
  }

  "POST /cluster/0/element/0 with test correlation id" should {
    "return 200" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        personalService_contractualObligationForSubstituteYes
      )
      val flowService = new InstrumentedIR35FlowService
      val result = new InterviewController(flowService,
        new TestSessionHelper(), SessionCacheHelper()).processElement(0, 0)(request).futureValue
      status(result) shouldBe Status.OK
      contentAsString(result) should include(personalService_contractualObligationInPractise)


//      result.session.data.contains(personalService_contractualObligationForSubstitute) shouldBe true

      flowService.passedCorrelationId shouldBe TEST_SESSION_ID

    }
  }

  val interview2 = interview1 :+ personalService_contractualObligationForSubstituteYes

//  "GET /cluster/0/element/0" should {
//    "result in the page being redisplayed and a blank interview from that point forward" in {
//
//      implicit val request = FakeRequest("GET", "/cluster/0/element/0").withSession(interview2 :_*)
//      val result = InterviewController().display(0,0)(request).futureValue
//
//      status(result) shouldBe Status.OK
//
//      result.session.data.contains(personalService_contractualObligationForSubstitute) shouldBe false
//
//      contentAsString(result) should include(personalService_contractualObligationForSubstitute)
//
//    }
//  }
}
