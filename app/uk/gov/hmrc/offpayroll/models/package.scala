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

import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.offpayroll.models.{DecisionRequest, DecisionResponse, QuestionAndAnswer, SessionInterview}
import uk.gov.hmrc.offpayroll.typeDefs.Interview
import uk.gov.hmrc.play.http.HeaderCarrier

import play.api.libs.json.{JsPath, Writes}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax


/**
  * Created by peter on 12/12/2016.
  */


package object modelsFormat {


  implicit val questionAnswerWrites = new Writes[QuestionAndAnswer] {
    def writes(qAndA: QuestionAndAnswer) = Json.obj(
      "questionTag" -> qAndA.questionTag,
      "answer" -> qAndA.answer
    )
  }

  implicit val questionAnswerReads: Reads[QuestionAndAnswer] = (
    (JsPath \ "questionTag").read[String] and
      (JsPath \ "answer").read[String]
    )(QuestionAndAnswer.apply _)


  val sessionInterviewWrites = new Writes[SessionInterview] {
    def writes(sessionInterview: SessionInterview) = Json.obj(
      "version" -> sessionInterview.version,
      "interview" -> sessionInterview.interview
    )
  }

  val sessionIntervieReads: Reads[SessionInterview] = (
    (JsPath \ "version").read[String] and
    (JsPath \ "interview").read[Seq[QuestionAndAnswer]]
    )(SessionInterview.apply _)


  implicit val decideRequestFormatter: Format[DecisionRequest] = Json.format[DecisionRequest]
  implicit val decideResponseFormatter: Format[DecisionResponse] = Json.format[DecisionResponse]
  implicit val interviewFormatter: Format[SessionInterview] = Format(sessionIntervieReads, sessionInterviewWrites)
  implicit val hc = HeaderCarrier()


}


package object typeDefs {

  type Interview = Map[String, String]
}