package uk.gov

import sbt.testing.Logger
import uk.gov.hmrc.offpayroll.FrontendSessionCacheConnector
import uk.gov.hmrc.offpayroll.connectors.SessionCacheConnector
import uk.gov.hmrc.offpayroll.controllers.OffPayrollController
import uk.gov.hmrc.offpayroll.models.{QuestionAndAnswer, SessionInterview}
import uk.gov.hmrc.offpayroll.modelsFormat._
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
class OffpayrollControllerSpec  extends UnitSpec with WithFakeApplication {


  implicit val hc = new HeaderCarrier(sessionId = Option(new SessionId("12345")))

  def found(sessionInterview: SessionInterview) = SessionInterview(sessionInterview.version, sessionInterview.interview)

  "An OffPayrollController " should {
    "be able to update or create entries in Keystore" in {

      val result = await(OffpayrollControllerTest.updateOrCreateInCache(found,
        () => SessionInterview("1", Seq(QuestionAndAnswer("hello", "world")))))

//      result.getEntry[SessionInterview]("sessionInterview").isEmpty shouldBe false
//      result.getEntry[SessionInterview]("sessionInterview").get.interview.contains("hello") shouldBe true
    }
  }

}

object OffpayrollControllerTest extends OffPayrollController {
  override val sessionCacheConnector: SessionCacheConnector = new FrontendSessionCacheConnector
}
