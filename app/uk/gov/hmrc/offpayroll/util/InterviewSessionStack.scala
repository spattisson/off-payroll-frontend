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

package uk.gov.hmrc.offpayroll.util

import play.api.mvc.Session
import uk.gov.hmrc.offpayroll.models.Element

import scala.util.Try

object InterviewSessionStack {
  val INTERVIEW_KEY = "interview"
  val INTERVIEW_CURRENT_INDEX = "index"

  def push(session: Session, values: String, element: Element): Session = {
    val compressed = session.data.getOrElse(INTERVIEW_KEY, "")
    val newInterview = InterviewStack.push(CompressedInterview(compressed), values, element)
    session + (INTERVIEW_KEY -> newInterview.str)
  }

  def pop(session: Session): (Session, String) = {
    val compressed = session.data.getOrElse(INTERVIEW_KEY, "")
    val (newInterview, questionTag) = InterviewStack.pop(CompressedInterview(compressed))
    (session + (INTERVIEW_KEY -> newInterview.str), questionTag)
  }

  def peek(session: Session): (Session, String) = {
    val compressed = session.data.getOrElse(INTERVIEW_KEY, "")
    val (_, questionTag) = InterviewStack.peek(CompressedInterview(compressed))
    (session + (INTERVIEW_KEY -> compressed), questionTag)
  }

  def reset(session: Session): Session =
    session - INTERVIEW_KEY

  def asMap(session: Session): Map[String, String] =
    session.data.get(INTERVIEW_KEY).map(CompressedInterview(_).asMap).getOrElse(Map())

  def asRawList(session: Session): List[(String, List[String])] =
    session.data.get(INTERVIEW_KEY).map(CompressedInterview(_).asRawList).getOrElse(List())

  def asList(session: Session): List[(String, String)] =
    session.data.get(INTERVIEW_KEY).map(CompressedInterview(_).asList).getOrElse(List())

  def addCurrentIndex(session: Session, currentElement: Element): Session =
    session + (INTERVIEW_CURRENT_INDEX -> InterviewStack.elementIndex(currentElement).fold("0")(_.toString))

  def currentIndex(session: Session): Element = {
    val maybeIndex = session.data.get(INTERVIEW_CURRENT_INDEX).flatMap{ indexStr => Try(indexStr.toInt).toOption}
    ElementProvider.toElements(maybeIndex.getOrElse(0))
  }
}
