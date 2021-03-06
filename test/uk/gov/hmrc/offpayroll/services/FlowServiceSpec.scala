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

package uk.gov.hmrc.offpayroll.services

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.offpayroll.connectors.DecisionConnector
import uk.gov.hmrc.offpayroll.models.DecisionResponse
import uk.gov.hmrc.offpayroll.modelsFormat._
import uk.gov.hmrc.offpayroll.resources._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by peter on 09/12/2016.
  */
class FlowServiceSpec extends UnitSpec with MockitoSugar with ServicesConfig with WithFakeApplication {

  private val TEST_CORRELATION_ID = "00000001099"

  private val decisionResponseString_inIr35 =
    """
      |{
      |  "version": "1.0.0-beta",
      |  "correlationID": "00000001099",
      |  "score": {
      |    "personalService": "HIGH",
      |    "control": "LOW",
      |    "financialRisk": "LOW",
      |    "partAndParcel": "LOW"
      |  },
      |  "result": "Inside IR35"
      |}
    """.stripMargin

  private val decisionResponseString_unknown =
    """
      |{
      |  "version": "1.0.0-beta",
      |  "correlationID": "00000001099",
      |  "score": {
      |    "personalService": "HIGH",
      |    "control": "LOW",
      |    "financialRisk": "",
      |    "partAndParcel": "MEDIUM"
      |  },
      |  "result": "Unknown"
      |}
    """.stripMargin


  private val jsonResponse_inIr35 = Json.fromJson[DecisionResponse](Json.parse(decisionResponseString_inIr35)).get
  private val jsonResponse_unknown = Json.fromJson[DecisionResponse](Json.parse(decisionResponseString_unknown)).get
  val mockDecisionConnector = mock[DecisionConnector]
  val testFlowService = new IR35FlowService(mockDecisionConnector)

  "A Flow Service should " should {
    " be able to get the start of an Interview" in {
      testFlowService.getStart(Map[String, String]()) should not be (null)
    }

    " move to the next cluster when cannotFixWorkerLocation is answered for control.workerDecideWhere" in {

      when(mockDecisionConnector.decide(any())(any())).thenReturn(Future(jsonResponse_unknown))

      val interview: Map[String, String] = Map(control_workerDecideWhere_cannotFixWorkerLocation)
      val currentElement: (String, String) = control_workerDecideWhere_cannotFixWorkerLocation

      val interviewEvalResult = await(testFlowService.evaluateInterview(interview, currentElement, TEST_CORRELATION_ID))

      interviewEvalResult.continueWithQuestions shouldBe true
      interviewEvalResult.element.head.questionTag shouldBe "financialRisk.haveToPayButCannotClaim"
      interviewEvalResult.correlationId shouldBe TEST_CORRELATION_ID
    }

    " Exit when Yes is answered for partParcel.workerAsLineManager as it would be the final question" in {

      when(mockDecisionConnector.decide(any())(any())).thenReturn(Future(jsonResponse_unknown))

      val interview: Map[String, String] = Map(partParcel_workerAsLineManager_yes)
      val currentElement: (String, String) = partParcel_workerAsLineManager_yes

      val interviewEvalResult = await(testFlowService.evaluateInterview(interview, currentElement, TEST_CORRELATION_ID))

      interviewEvalResult.continueWithQuestions shouldBe false
      interviewEvalResult.correlationId shouldBe TEST_CORRELATION_ID
    }

    " be able to process a partial personalService and expect it to return Continue" in {

      when(mockDecisionConnector.decide(any())(any())).thenReturn(Future(jsonResponse_unknown))

      val interview: Map[String, String] = Map(personalService_workerSentActualSubstituteYesClientAgreed)
      val currentElement: (String, String) = personalService_workerSentActualSubstituteYesClientAgreed

      val interviewEvalResult = await(testFlowService.evaluateInterview(interview, currentElement, TEST_CORRELATION_ID))

      assert(interviewEvalResult.continueWithQuestions === true, "Only a partial personalService so we need to continue")
      assert(interviewEvalResult.element.head.questionTag === personalService_workerPayActualSubstitute) //next tag
      interviewEvalResult.correlationId shouldBe TEST_CORRELATION_ID
    }

    " be able to get the current currentElement" in {
      assert(testFlowService.getAbsoluteElement(0, 1).questionTag == personalService_workerPayActualSubstitute)
    }
  }


}
