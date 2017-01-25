package uk.gov

import sbt.testing.Logger
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.offpayroll.{FrontendAppConfig, FrontendSessionCacheConnector}
import uk.gov.hmrc.offpayroll.connectors.{SessionCacheConnector, SessionCacheHelper}
import uk.gov.hmrc.offpayroll.controllers.OffPayrollController
import uk.gov.hmrc.offpayroll.models.{QuestionAndAnswer, SessionInterview}
import uk.gov.hmrc.offpayroll.modelsFormat.interviewFormatter
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by peter on 17/01/2017.
  *
  * Needs a Session Store (Mongo Instance running) GET of 'http://localhost:27017/keystore/off-payroll-frontend/12345'
  * sm --start KEYSTORE
  *
  */
class SessionCacheHelperSpec extends UnitSpec with WithFakeApplication {

  implicit val hc = new HeaderCarrier(sessionId = Option(new SessionId("12345")))

  val sessionInterview = SessionInterview("1.0", Seq(QuestionAndAnswer("hello", "world")))

  "An SessionCacheHelper " should {
    "be able to update or create entries in Keystore" in {

      val result = await(new SessionCacheHelper(new FrontendSessionCacheConnector).createNew)

      val sessionInterview: Option[SessionInterview] = checkExtractSessionInteview(result)
      sessionInterview.get.interview.isEmpty shouldBe true

    }
  }

  private def checkExtractSessionInteview(result: CacheMap) = {
    val sessionInterview = result.getEntry("sessionInterview")
    sessionInterview.isEmpty shouldBe false
    sessionInterview.get.version shouldBe FrontendAppConfig.decisionServiceSchemaVersion
    sessionInterview
  }

  "Be able to add a single entry" in {

    await(new SessionCacheHelper(new FrontendSessionCacheConnector).createNew)
    val result = await(new SessionCacheHelper(new FrontendSessionCacheConnector).addEntry("someTag", "no"))

    val sessionInterview: Option[SessionInterview] = checkExtractSessionInteview(result)

    val interview = sessionInterview.get.interview
    interview.isEmpty shouldBe false
    interview.size shouldBe 1

    interview.exists(qAndA => qAndA.questionTag == "someTag" && qAndA.answer == "no") shouldBe true

  }

  "Be able to update a single entry" in {

    await(new SessionCacheHelper(new FrontendSessionCacheConnector).createNew)
    await(new SessionCacheHelper(new FrontendSessionCacheConnector).addEntry("someTag", "no"))
    val result = await(new SessionCacheHelper(new FrontendSessionCacheConnector).addEntry("someTag", "yes"))

    val sessionInterview: Option[SessionInterview] = checkExtractSessionInteview(result)
    sessionInterview.get.interview.isEmpty shouldBe false
    sessionInterview.get.interview.size shouldBe 1
    sessionInterview.get.interview.exists(qAndA => qAndA.questionTag == "someTag" && qAndA.answer == "no") shouldBe false
    sessionInterview.get.interview.exists(qAndA => qAndA.questionTag == "someTag" && qAndA.answer == "yes") shouldBe true
  }

}

