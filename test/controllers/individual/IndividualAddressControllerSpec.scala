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
import config.FrontendAppConfig
import forms.AddressFormProvider
import matchers.JsonMatchers
import models.Address._
import models.{Address, Country, NormalMode, UnsubmittedDisclosure, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.IndividualAddressPage
import pages.unsubmitted.UnsubmittedDisclosurePage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CountryListFactory

import scala.concurrent.Future

class IndividualAddressControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockCountryFactory: CountryListFactory = mock[CountryListFactory]

  val formProvider = new AddressFormProvider()
  val form: Form[Address] = formProvider(Seq(Country("valid","FR","France")))

  val validAnswer: Address = Address(Some("value 1"),Some("value 2"),Some("value 3"),"value 4",Some("XX9 9XX"),
    Country("valid","FR","France"))

  val validData =
    Map(
      "addressLine1" -> "value 1",
      "addressLine2" -> "value 2",
      "addressLine3" -> "value 3",
      "city"         -> "value 4",
      "postCode"     -> "XX9 9XX",
      "country"      -> "FR"
    )

  lazy val individualAddressRoute: String = controllers.individual.routes.IndividualAddressController.onPageLoad(0, NormalMode).url

  "OrganisationAddress Controller" - {

    //TODO: Add test for redirect to check-your-answers when page is created

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockCountryFactory.getCountryList()).thenReturn(Some(Seq(Country("valid","FR","France"))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[CountryListFactory].toInstance(mockCountryFactory)).build()

      val request = FakeRequest(GET, individualAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> form,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "address.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockCountryFactory.getCountryList()).thenReturn(Some(Seq(Country("valid","FR","France"))))

      val userAnswers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(IndividualAddressPage, 0, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[CountryListFactory].toInstance(mockCountryFactory)).build()
      val request = FakeRequest(GET, individualAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(validData)

      val expectedJson = Json.obj(
        "form" -> filledForm,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "address.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()


      val request =
        FakeRequest(POST, individualAddressRoute)
          .withFormUrlEncodedBody(validData.toList:_*)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, individualAddressRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode
      )

      templateCaptor.getValue mustEqual "address.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, individualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, individualAddressRoute)
          .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
