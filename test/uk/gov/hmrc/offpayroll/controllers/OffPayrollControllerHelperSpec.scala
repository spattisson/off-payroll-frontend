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

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.offpayroll.models.{Element, _}

class OffPayrollControllerHelperSpec extends FlatSpec with Matchers {

  val testOffpayrollController = new TestOffPayrollControllerHelper

  var cluster = PersonalServiceCluster

  private val tag1 = PersonalServiceCluster.clusterElements.head.questionTag
  val element = PersonalServiceCluster.clusterElements.head


  val form = testOffpayrollController.createForm(element)

  "An OffPayrollControllerHelper " should "create a String based form for a given element " in {
    val boundForm = form.bind(Map(tag1  + "[0]" -> "perhaps"))
    boundForm.data.contains(tag1  + "[0]") shouldBe true
    boundForm.errors.headOption.isDefined shouldBe false
  }

  it should "be in error if the bound data can't be matched" in {
    val boundForm = form.bind(Map("nonExistentTag" -> "perhaps"))
    boundForm.errors.headOption.isDefined shouldBe true
  }



}

class TestOffPayrollControllerHelper extends OffPayrollControllerHelper