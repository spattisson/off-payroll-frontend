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

package uk.gov.hmrc.offpayroll.models

import uk.gov.hmrc.offpayroll.models.FinancialRiskBCluster.{findNextQuestion, getElementForQuestionTag}

object PartAndParcelCluster extends Cluster {

  /**
    * Use this value to informatively name the cluster and use as a key to tags
    */
  override def name: String = "partParcel"

  override def clusterID: Int = 4

  val clusterElements: List[Element] = List(
    Element("workerReceivesBenefits", RADIO, 0, this),
    Element("workerAsLineManager", RADIO, 1, this),
    Element("contactWithEngagerCustomer", RADIO, 2, this),
    Element("workerRepresentsEngagerBusiness", RADIO, 3, this)
  )

  private val flows = List(
    FlowElement("partParcel.workerReceivesBenefits",
      Map("partParcel.workerReceivesBenefits" -> "No"),
      Option("partParcel.workerAsLineManager")),
    FlowElement("partParcel.workerReceivesBenefits",
      Map("partParcel.workerReceivesBenefits" -> "Yes"),
      Option.empty),
    FlowElement("partParcel.workerAsLineManager",
      Map("partParcel.workerReceivesBenefits" -> "No", "partParcel.workerAsLineManager" -> "Yes"),
      Option.empty),
    FlowElement("partParcel.workerAsLineManager",
      Map("partParcel.workerReceivesBenefits" -> "No", "partParcel.workerAsLineManager" -> "No"),
      Option("partParcel.contactWithEngagerCustomer")),
    FlowElement("partParcel.contactWithEngagerCustomer",
      Map("partParcel.workerReceivesBenefits" -> "No", "partParcel.workerAsLineManager" -> "No",
        "partParcel.contactWithEngagerCustomer" -> "Yes"),
      Option("partParcel.workerRepresentsEngagerBusiness")),
    FlowElement("partParcel.contactWithEngagerCustomer",
      Map("partParcel.workerReceivesBenefits" -> "No", "partParcel.workerAsLineManager" -> "No",
        "partParcel.contactWithEngagerCustomer" -> "No"),
      Option.empty)
  )

  /**
    * Returns the next element in the cluster or empty if we should ask for a decision
    *
    * @param clusterAnswers
    * @return
    */
  override def shouldAskForDecision(clusterAnswers: List[(String, String)], currentQnA: (String, String)): Option[Element] = {
    if (allQuestionsAnswered(clusterAnswers))Option.empty
    else
      getNextQuestionTag(clusterAnswers, currentQnA)

  }

  def getNextQuestionTag(clusterAnswers: List[(String, String)], currentQnA: (String, String)): Option[Element] = {
    val currentQuestionFlowElements = flows.filter(_.currentQuestion.equalsIgnoreCase(currentQnA._1))
    val relevantFlowElement = currentQuestionFlowElements.filter{
      element => element.answers.forall(clusterAnswers.contains(_))
    }
    if(relevantFlowElement.isEmpty) findNextQuestion(currentQnA)
    else
      getElementForQuestionTag(relevantFlowElement.head.nextQuestion.getOrElse(""))
  }
}