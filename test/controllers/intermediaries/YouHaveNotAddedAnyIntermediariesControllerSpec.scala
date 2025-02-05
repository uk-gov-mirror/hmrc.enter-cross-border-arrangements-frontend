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

package controllers.intermediaries

import base.SpecBase
import forms.intermediaries.YouHaveNotAddedAnyIntermediariesFormProvider
import matchers.JsonMatchers
import models.intermediaries.{Intermediary, WhatTypeofIntermediary, YouHaveNotAddedAnyIntermediaries}
import models.organisation.Organisation
import models.taxpayer.TaxResidency
import models.{Country, IsExemptionKnown, NormalMode, UnsubmittedDisclosure, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.intermediaries.{IntermediaryLoopPage, YouHaveNotAddedAnyIntermediariesPage}
import pages.unsubmitted.UnsubmittedDisclosurePage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class YouHaveNotAddedAnyIntermediariesControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute = Call("GET", "/foo")

  lazy val youHaveNotAddedAnyIntermediariesRoute = routes.YouHaveNotAddedAnyIntermediariesController.onPageLoad(0).url

  val formProvider = new YouHaveNotAddedAnyIntermediariesFormProvider()
  val form = formProvider()

  "YouHaveNotAddedAnyIntermediaries Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any())) thenReturn Future.successful(Html(""))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(GET, youHaveNotAddedAnyIntermediariesRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"       -> form,
        "mode"       -> NormalMode,
        "intermediaryList" -> Json.arr(),
        "radios" -> YouHaveNotAddedAnyIntermediaries.radios(form)
      )

      templateCaptor.getValue mustEqual "intermediaries/youHaveNotAddedAnyIntermediaries.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    //TODO Include test for change and remove links if needed
    "must return OK and the correct view with the list of all intermediaries for a GET" ignore {

      when(mockRenderer.render(any(), any())(any())) thenReturn Future.successful(Html(""))

      val organisation = Organisation(
        organisationName = "Organisation Ltd.",
        address = None,
        emailAddress = None,
        taxResidencies = IndexedSeq(TaxResidency(Some(Country("", "GB", "United Kingdom")), None))
      )

      val intermediariesLoop =
        IndexedSeq(Intermediary("id", None, Some(organisation), WhatTypeofIntermediary.Promoter, IsExemptionKnown.No, None, None))
      val userAnswers = UserAnswers(userAnswersId).set(IntermediaryLoopPage, 0, intermediariesLoop).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request = FakeRequest(GET, youHaveNotAddedAnyIntermediariesRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedList = Json.arr(Json.obj("name" -> "Organisation Ltd.", "changeUrl" -> "#", "removeUrl" -> "#"))

      val expectedJson = Json.obj(
        "form"       -> form,
        "mode"       -> NormalMode,
        "intermediaryList" -> expectedList,
        "radios" -> YouHaveNotAddedAnyIntermediaries.radios(form)
      )

      templateCaptor.getValue mustEqual "intermediaries/youHaveNotAddedAnyIntermediaries.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any())) thenReturn Future.successful(Html(""))

      val userAnswers = UserAnswers(userAnswersId)
        .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
        .set(YouHaveNotAddedAnyIntermediariesPage, 0, YouHaveNotAddedAnyIntermediaries.values.head).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request = FakeRequest(GET, youHaveNotAddedAnyIntermediariesRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.fill(YouHaveNotAddedAnyIntermediaries.values.head)

      val expectedJson = Json.obj(
        "form"       -> filledForm,
        "mode"       -> NormalMode,
        "radios" -> YouHaveNotAddedAnyIntermediaries.radios(filledForm)
      )

      templateCaptor.getValue mustEqual "intermediaries/youHaveNotAddedAnyIntermediaries.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, youHaveNotAddedAnyIntermediariesRoute)
          .withFormUrlEncodedBody(("value", YouHaveNotAddedAnyIntermediaries.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "/disclose-cross-border-arrangements/manual/intermediaries/type/0"

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request =  FakeRequest(POST, youHaveNotAddedAnyIntermediariesRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"       -> boundForm,
        "mode"       -> NormalMode,
        "radios" -> YouHaveNotAddedAnyIntermediaries.radios(boundForm)
      )

      templateCaptor.getValue mustEqual "intermediaries/youHaveNotAddedAnyIntermediaries.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }
  }
}
