/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import base.SpecBase
import controllers.Assets.SERVICE_UNAVAILABLE
import generators.Generators
import helpers.JsonFixtures._
import models.subscription._
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, JsValue}
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionConnectorSpec extends SpecBase
  with ScalaCheckPropertyChecks
  with Generators with BeforeAndAfterEach {

  val primaryContact: PrimaryContact = PrimaryContact(Seq(
    ContactInformationForIndividual(
      individual = IndividualDetails(firstName = "FirstName", lastName = "LastName", middleName = None),
      email = "email@email.com", phone = Some("07111222333"), mobile = Some("07111222333"))
  ))
  val secondaryContact: SecondaryContact = SecondaryContact(Seq(
    ContactInformationForOrganisation(
      organisation = OrganisationDetails(organisationName = "Organisation Name"),
      email = "email@email.com", phone = None, mobile = None)
  ))

  val responseCommon: ResponseCommon = ResponseCommon(
    status = "OK",
    statusText = None,
    processingDate = "2020-08-09T11:23:45Z",
    returnParameters = None)

  val responseDetail: ResponseDetail = ResponseDetail(
    subscriptionID = "XE0001234567890",
    tradingName = Some("Trading Name"),
    isGBUser = true,
    primaryContact = primaryContact,
    secondaryContact = Some(secondaryContact))

  val enrolmentID: String = "1234567890"

  val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
    DisplaySubscriptionForDACResponse(
      SubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetail)
    )

  val mockHttpClient: HttpClient = mock[HttpClient]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[HttpClient].toInstance(mockHttpClient)
    ).build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  override def beforeEach {
    Mockito.reset(mockHttpClient)
  }

  "SubscriptionConnector" - {

    "displaySubscriptionDetails" - {
      "must return the correct DisplaySubscriptionForDACResponse" in {
        forAll(validDisclosureID) {
          safeID =>
            val expectedBody = displaySubscriptionPayload(
              JsString(safeID), JsString("FirstName"), JsString("LastName"), JsString("Organisation Name"),
              JsString("email@email.com"), JsString("email@email.com"), JsString("07111222333"))

            val responseDetailUpdate: ResponseDetail = responseDetail.copy(subscriptionID = safeID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                SubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailUpdate)
              )

            when(mockHttpClient.POST[JsValue, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
              .thenReturn(Future.successful(HttpResponse(OK, expectedBody)))


            val result = connector.displaySubscriptionDetails(enrolmentID)
            result.futureValue mustBe Some(displaySubscriptionForDACResponse)
        }
      }

      "must return None if unable to validate json" in {
        forAll(validDisclosureID) {
          safeID =>
            val invalidBody =
              s"""
                 |{
                 |  "displaySubscriptionForDACResponse": {
                 |    "responseCommon": {
                 |      "processingDate": "2020-08-09T11:23:45Z"
                 |    },
                 |    "responseDetail": {
                 |      "subscriptionID": "$safeID",
                 |      "tradingName": "Trading Name",
                 |      "isGBUser": true,
                 |      "primaryContact": [
                 |        {
                 |          "email": "email@email.com",
                 |          "individual": {
                 |            "lastName": "LastName",
                 |            "firstName": "FirstName"
                 |          }
                 |        }
                 |      ]
                 |    }
                 |  }
                 |}""".stripMargin

            when(mockHttpClient.POST[JsValue, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
              .thenReturn(Future.successful(HttpResponse(OK, invalidBody)))

            val result = connector.displaySubscriptionDetails(enrolmentID)
            result.futureValue mustBe None
        }
      }

      "must return None if status is not OK" in {
        when(mockHttpClient.POST[JsValue, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE, "")))

        val result = connector.displaySubscriptionDetails(enrolmentID)
        result.futureValue mustBe None
      }
    }

  }

}
