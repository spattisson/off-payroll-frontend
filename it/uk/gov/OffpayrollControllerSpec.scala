package uk.gov

import sbt.testing.Logger
import uk.gov.hmrc.offpayroll.FrontendSessionCacheConnector
import uk.gov.hmrc.offpayroll.connectors.SessionCacheConnector
import uk.gov.hmrc.offpayroll.controllers.OffPayrollController
import uk.gov.hmrc.offpayroll.models.SessionInterview
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

  def found(interview: SessionInterview) = SessionInterview(interview.interview + ("foo" -> "bar"))

  "An OffPayrollController " should {
    "be able to update or create entries in Keystore" in {

      val result = await(OffpayrollControllerTest.updateOrCreateInCache(found,
        () => SessionInterview(Map("hello" -> "world"))))

      result.getEntry[SessionInterview]("interview").isEmpty shouldBe false
      result.getEntry[SessionInterview]("interview").get.interview.contains("hello") shouldBe true
    }
  }

}

object OffpayrollControllerTest extends OffPayrollController {
  override val sessionCacheConnector: SessionCacheConnector = FrontendSessionCacheConnector
}
