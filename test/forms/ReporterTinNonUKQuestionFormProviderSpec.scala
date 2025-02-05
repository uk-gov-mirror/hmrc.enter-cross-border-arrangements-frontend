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

package forms

import forms.behaviours.BooleanFieldBehaviours
import forms.reporter.ReporterTinNonUKQuestionFormProvider
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class ReporterTinNonUKQuestionFormProviderSpec extends BooleanFieldBehaviours with GuiceOneAppPerSuite {

  val requiredKey = "Select yes if you know any tax identification numbers for France"
  val invalidKey = "error.boolean"

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val defaultMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val messages: Messages = defaultMessagesApi.preferred(request)

  val formProvider = new ReporterTinNonUKQuestionFormProvider()
  val form: Form[Boolean] = formProvider("France")(messages)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
