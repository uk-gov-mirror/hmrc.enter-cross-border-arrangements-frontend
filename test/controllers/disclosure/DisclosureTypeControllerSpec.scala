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

package controllers.disclosure

import base.SpecBase
import connectors.HistoryConnector
import forms.disclosure.DisclosureTypeFormProvider
import matchers.JsonMatchers
import models.disclosure.DisclosureType
import models.{NormalMode, UnsubmittedDisclosure, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.disclosure.DisclosureTypePage
import pages.unsubmitted.UnsubmittedDisclosurePage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future


class DisclosureTypeControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  lazy val disclosureTypeRoute = controllers.disclosure.routes.DisclosureTypeController.onPageLoad(NormalMode).url

  val formProvider = new DisclosureTypeFormProvider()
  val form = formProvider()

  val mockConnector = mock[HistoryConnector]

  "DisclosureType Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockConnector.getSubmissionDetails(any())(any())).thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[HistoryConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, disclosureTypeRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> form,
        "mode"   -> NormalMode,
        "radios" -> DisclosureType.radios(form)
      )

      templateCaptor.getValue mustEqual "disclosure/disclosureType.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockConnector.getSubmissionDetails(any())(any())).thenReturn(Future.successful(false))

      val userAnswers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .setBase(DisclosureTypePage, DisclosureType.values.head).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[HistoryConnector].toInstance(mockConnector))
            .build()
      val request = FakeRequest(GET, disclosureTypeRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> DisclosureType.values.head.toString))

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "mode"   -> NormalMode,
        "radios" -> DisclosureType.radios(filledForm)
      )

      templateCaptor.getValue mustEqual "disclosure/disclosureType.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockConnector.getSubmissionDetails(any())(any())).thenReturn(Future.successful(false))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[HistoryConnector].toInstance(mockConnector)
          )
          .build()

      val request =
        FakeRequest(POST, disclosureTypeRoute)
          .withFormUrlEncodedBody(("value", DisclosureType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockConnector.getSubmissionDetails(any())(any())).thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
                  .overrides(bind[HistoryConnector].toInstance(mockConnector))
                  .build()
      val request = FakeRequest(POST, disclosureTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode,
        "radios" -> DisclosureType.radios(boundForm)
      )

      templateCaptor.getValue mustEqual "disclosure/disclosureType.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }
  }
}
