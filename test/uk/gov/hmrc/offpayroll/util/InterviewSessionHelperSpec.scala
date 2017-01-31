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

import org.scalatest.{FlatSpec, Matchers}
import play.api.mvc.Session
import uk.gov.hmrc.offpayroll.util.InterviewSessionHelper.INTERVIEW_KEY

/**
  * Created by peter on 26/01/2017.
  */
class InterviewSessionHelperSpec extends FlatSpec with Matchers {

  val mockSession = Session.deserialize(Map())

  "An InterviewSessionHelper " should "take an existing Session and add a new value on an interview " in {
    val newSession = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    newSession.data.keys should contain(INTERVIEW_KEY)
  }

  val someInterview = "someQuestionTag:someAnswer;someOtherQuestionTag:someOtherAnswer"

  it should "be able to add a second value" in {
    val newSession  = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    val finalSession = InterviewSessionHelper.push(newSession, "someOtherQuestionTag", "someOtherAnswer")
    finalSession.data.keys should contain(INTERVIEW_KEY)
    finalSession(INTERVIEW_KEY) shouldBe someInterview
  }

  it should "replace the value of an existing key" in {
    val newSession = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    val finalSession = InterviewSessionHelper.push(newSession, "someQuestionTag", "someOtherAnswer")

    finalSession(INTERVIEW_KEY) shouldBe "someQuestionTag:someOtherAnswer"
  }

  it should "convert a string encoded interview to a Map of String String" in {
    val newSession = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    InterviewSessionHelper.asMap(newSession) shouldBe Map(("someQuestionTag" -> "someAnswer" ))
  }

  it should "pop the last value from the interview and return a new session without the last value" in {
    val newSession = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    val finalSession = InterviewSessionHelper.push(newSession, "someOtherQuestionTag", "someOtherAnswer")

    val (updatedSession, questionTag) = InterviewSessionHelper.pop(finalSession)

    questionTag shouldBe "someOtherQuestionTag"
    InterviewSessionHelper.asMap(updatedSession) shouldBe Map(("someQuestionTag" -> "someAnswer" ))
  }

  it should "pop should work on an empty interview" in {
    val (updatedSession, questionTag) = InterviewSessionHelper.pop(mockSession)
    questionTag shouldBe ""
    InterviewSessionHelper.asMap(updatedSession) shouldBe Map()
  }

  it should "reset an interview which is not empty" in {
    val newSession = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    InterviewSessionHelper.asMap(newSession) shouldBe Map(("someQuestionTag" -> "someAnswer" ))
    val resetSession = InterviewSessionHelper.reset(newSession)
    InterviewSessionHelper.asMap(resetSession) shouldBe Map()
    val afterResetSession = InterviewSessionHelper.push(resetSession, "someQuestionTag", "someAnswer")
    InterviewSessionHelper.asMap(afterResetSession) shouldBe Map(("someQuestionTag" -> "someAnswer" ))
  }

  it should "reset an empty interview" in {
    val resetSession = InterviewSessionHelper.reset(mockSession)
    InterviewSessionHelper.asMap(resetSession) shouldBe Map()
  }

  it should "provide unchanged session and top element of a non empty interview" in {
    val session1 = InterviewSessionHelper.push(mockSession, "someQuestionTag", "someAnswer")
    val session2 = InterviewSessionHelper.push(session1, "someQuestionTag2", "someAnswer2")
    InterviewSessionHelper.asMap(session2) shouldBe Map("someQuestionTag" -> "someAnswer", "someQuestionTag2" -> "someAnswer2")
    val (peekSession, topTag) = InterviewSessionHelper.peek(session2)
    InterviewSessionHelper.asMap(peekSession) shouldBe Map("someQuestionTag" -> "someAnswer", "someQuestionTag2" -> "someAnswer2")
    topTag shouldBe "someQuestionTag2"
  }

  it should "provide unchanged session and empty top element of an empty interview" in {
    val (peekSession, topTag) = InterviewSessionHelper.peek(mockSession)
    println(topTag)
    InterviewSessionHelper.asMap(peekSession) shouldBe Map()
    topTag shouldBe ""
  }

  it should "pop the values from the interview until (excluding) the given question tag" in {
    val newSession = InterviewSessionHelper.push(mockSession, "t1", "a1")
    val finalSession = InterviewSessionHelper.push(newSession, "t2", "a2")

    val maybeSessionTag = InterviewSessionHelper.popTo(finalSession, "t2")
    maybeSessionTag.isDefined shouldBe true
    maybeSessionTag.map { case (updatedSession, questionTag) =>
      questionTag shouldBe "t1"
      InterviewSessionHelper.asMap(updatedSession) shouldBe Map("t1" -> "a1" )
    }
  }

  it should "pop the values from the interview (until) returns none if tag is not found" in {
    val newSession = InterviewSessionHelper.push(mockSession, "t1", "a1")
    val finalSession = InterviewSessionHelper.push(newSession, "t2", "a2")

    val maybeSessionTag = InterviewSessionHelper.popUntil(finalSession, "t3")
    maybeSessionTag.isDefined shouldBe false
  }

  it should "pop the values from the interview to (including) the given question tag" in {
    val newSession = InterviewSessionHelper.push(mockSession, "t1", "a1")
    val finalSession = InterviewSessionHelper.push(newSession, "t2", "a2")

    val maybeSessionTag = InterviewSessionHelper.popUntil(finalSession, "t2")
    maybeSessionTag.isDefined shouldBe true
    maybeSessionTag.map { case (updatedSession, questionTag) =>
      questionTag shouldBe "t2"
      InterviewSessionHelper.asMap(updatedSession) shouldBe Map("t1" -> "a1", "t2" -> "a2")
    }
  }

  it should "pop the values from the interview to (including) the given question tag leaving interview empty if needed" in {
    val newSession = InterviewSessionHelper.push(mockSession, "t1", "a1")
    val finalSession = InterviewSessionHelper.push(newSession, "t2", "a2")

    val maybeSessionTag = InterviewSessionHelper.popUntil(finalSession, "t1")
    maybeSessionTag.isDefined shouldBe true
    maybeSessionTag.map { case (updatedSession, questionTag) =>
      questionTag shouldBe "t1"
      InterviewSessionHelper.asMap(updatedSession) shouldBe Map("t1" -> "a1")
    }
  }

  it should "pop the values from the interview (to) returns none if tag is not found" in {
    val newSession = InterviewSessionHelper.push(mockSession, "t1", "a1")
    val finalSession = InterviewSessionHelper.push(newSession, "t2", "a2")

    val maybeSessionTag = InterviewSessionHelper.popUntil(finalSession, "t3")
    maybeSessionTag.isDefined shouldBe false
  }

}
