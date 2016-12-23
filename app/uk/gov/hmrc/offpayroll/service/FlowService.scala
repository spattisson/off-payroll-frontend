/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.offpayroll.service

import play.api.Logger
import uk.gov.hmrc.offpayroll.connectors.DecisionConnector
import uk.gov.hmrc.offpayroll.models.{UNKNOWN, _}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.offpayroll.DecisionConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by peter on 09/12/2016.
  */
abstract class FlowService {

  /**
    *
    * @param interview
    * @return
    */
  def evaluateInterview(interview: Map[String, String], currentQnA: (String, String)): Future[InterviewEvaluation]

  def getStart(): Element

  def getAbsoluteElement(clusterId: Int, elementId: Int): Element

}


object IR35FlowService extends FlowService {

  private val STOP = false
  private val CONTINUE = true
  implicit val hc = HeaderCarrier()

  private val weblow: Webflow = OffPayrollWebflow

  lazy val decisionConnector: DecisionConnector = DecisionConnector

  override def getStart(): Element = weblow.getStart()

  private def guardValidEelement(currentTag: String): Element = {
    val tag = weblow.getElementByTag(currentTag)
    if (tag.isEmpty) throw new IllegalAccessException("No Such Element: " + currentTag)
    else tag.get
  }

  private def getStatus(decision: DecisionResponse):DecisionType = decision.result match {
    case "Outside IR35" => OUT
    case "Inside IR35" => IN
    case "Unknown" => UNKNOWN
    case _ => UNKNOWN
  }

  override def evaluateInterview(interview: Map[String, String], currentQnA: (String, String)): Future[InterviewEvaluation] = {

    val cleanInterview = interview.filter(qa => weblow.clusters().exists(clsrt => qa._1.startsWith(clsrt.name)))
    val currentTag = currentQnA._1
    val currentElement: Element = guardValidEelement(currentTag)
    val optionalNextElement = weblow.shouldAskForDecision(interview, currentQnA)

    if (optionalNextElement.isEmpty) {
      decisionConnector.decide(DecisionBuilder.buildDecisionRequest(cleanInterview)).map[InterviewEvaluation](
        decision => {
          Logger.debug("Decision received from Decision Service: " + decision)
            if (getStatus(decision) == UNKNOWN) {
              if (weblow.getNext(currentElement).isEmpty) {
                InterviewEvaluation(Option.empty[Element], Option(Decision(cleanInterview, UNKNOWN)), STOP)
              }
              else
                InterviewEvaluation(weblow.getNext(currentElement), Option.empty[Decision], CONTINUE)
            } else {
                InterviewEvaluation(Option.empty[Element], Option.apply(Decision(cleanInterview, getStatus(decision))), STOP)
            }
          }
          )
    } else {
      Future[InterviewEvaluation](InterviewEvaluation(optionalNextElement, Option.empty[Decision], CONTINUE))
    }
  }


  override def getAbsoluteElement(clusterId: Int, elementId: Int): Element = {
    val currentElement = weblow.getEelmentById(clusterId, elementId)
    if (currentElement.nonEmpty) currentElement.head
    else throw new NoSuchElementException("No Element found matching: " + clusterId + "/" + elementId)
  }

}

case class InterviewEvaluation(element: Option[Element], decision: Option[Decision], continueWithQuestions: Boolean) {

}
