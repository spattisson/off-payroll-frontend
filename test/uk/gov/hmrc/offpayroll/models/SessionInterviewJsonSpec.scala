package uk.gov.hmrc.offpayroll.models

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

import uk.gov.hmrc.offpayroll.modelsFormat._

/**
  * Created by peter on 24/01/2017.
  */
class SessionInterviewJsonSpec extends FlatSpec with Matchers {

  private val sessionInterviewJson =
    """
      |{
      |  "version": "1.0.1-beta",
      |  "interview": [
      |   {"questionTag": "personalService.contractualObligationForSubstitute", "answer": "yes" },
      |   {"questionTag": "personalService.contractualObligationInPractise", "answer": "no" }
      |  ]
      |}
    """.stripMargin


  "A SessionInterview Json Formatter " should "parse Json into a SessionInterview Object" in {
    val sessionInterview = Json.fromJson[SessionInterview](Json.parse(sessionInterviewJson))

    sessionInterview.isSuccess shouldBe true

    sessionInterview.get.interview.exists(qNa =>
      qNa.questionTag == "personalService.contractualObligationForSubstitute" &&
        qNa.answer == "yes") shouldBe true

    sessionInterview.get.interview.exists(qNa =>
      qNa.questionTag == "personalService.contractualObligationInPractise" &&
        qNa.answer == "no") shouldBe true
  }

  it should "deserialize from Json " in {
    val sessionInterview = SessionInterview("1.0.1-beta",
      Seq(QuestionAndAnswer("personalService.contractualObligationForSubstitute", "yes"),
        QuestionAndAnswer("personalService.contractualObligationInPractise", "no")))

      assert(Json.toJson(sessionInterview) === Json.parse(sessionInterviewJson),
        "From Object and String should be the same")
  }

}
