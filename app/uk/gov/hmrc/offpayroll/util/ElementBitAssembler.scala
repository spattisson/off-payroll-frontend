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

import uk.gov.hmrc.offpayroll.models.{Element, GROUP, MULTI, RADIO}

object ElementBitAssembler {

  private def decodeMulti(bitValue: Int, element: Element): List[String] = {
    val maybeQuestionTag = element.children.find(_.order + 1 == bitValue).map(_.questionTag)
    maybeQuestionTag.toList
  }

  private def decodeGroup(bitValue: Int, element: Element): List[String] = {
    val tags = element.children.collect { case a if (bitValue & (1 << a.order)) > 0 => a }
    tags.map(_.questionTag)
  }

  private def decodeYesNo(bitValue: Int) = List(Nil, List("No"), List("Yes"), Nil)(bitValue & 0x11)

  def fromBitElement(bitValue: Int, element: Element): List[String] = element.elementType match {
    case RADIO => decodeYesNo(bitValue)
    case MULTI => decodeMulti(bitValue, element)
    case GROUP => decodeGroup(bitValue, element)
  }
}