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

package controllers.individual

import base.SpecBase
import connectors.AddressLookupConnector
import forms.SelectAddressFormProvider
import matchers.JsonMatchers
import models.{AddressLookup, NormalMode, UnsubmittedDisclosure, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.{IndividualSelectAddressPage, IndividualUkPostcodePage}
import pages.unsubmitted.UnsubmittedDisclosurePage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class IndividualSelectAddressControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  private val mockAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  lazy private val selectAddressRoute = controllers.individual.routes.IndividualSelectAddressController.onPageLoad(0, NormalMode).url
  lazy private val manualAddressURL: String = controllers.individual.routes.IndividualAddressController.onPageLoad(0, NormalMode).canonical()

  private val formProvider = new SelectAddressFormProvider()
  private val form = formProvider()

  private val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )
  private val addressRadios: Seq[Radios.Radio] = Seq(
    Radios.Radio(label = msg"1 Address line 1, Town, ZZ1 1ZZ", value = s"1 Address line 1, Town, ZZ1 1ZZ"),
    Radios.Radio(label = msg"2 Address line 1, Town, ZZ1 1ZZ", value = s"2 Address line 1, Town, ZZ1 1ZZ")
  )

  "SelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))

      val answers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(IndividualUkPostcodePage, 0, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(answers)).overrides(
        bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()

      val request = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> form,
        "mode"   -> NormalMode,
        "manualAddressURL" -> manualAddressURL,
        "radios" -> Radios(field = form("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(IndividualSelectAddressPage, 0, "1 Address line 1, Town, ZZ1 1ZZ")
        .success
        .value
        .set(IndividualUkPostcodePage, 0, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()
      val request = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> addressRadios.head.value))

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "mode"   -> NormalMode,
        "manualAddressURL" -> manualAddressURL,
        "radios" -> Radios(field = filledForm("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))

      val answers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(IndividualUkPostcodePage, 0, "ZZ1 1ZZ")
        .success
        .value


      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          ).build()

      val request =
        FakeRequest(POST, selectAddressRoute)
          .withFormUrlEncodedBody(("value", "1 Address line 1, Town, ZZ1 1ZZ"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "/disclose-cross-border-arrangements/manual/individual/email-address/0"

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))

      val answers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(IndividualUkPostcodePage, 0, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()

      val request = FakeRequest(POST, selectAddressRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode,
        "manualAddressURL" -> manualAddressURL,
        "radios" -> Radios(field = boundForm("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, selectAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, selectAddressRoute)
          .withFormUrlEncodedBody(("value", addressRadios.head.value))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
