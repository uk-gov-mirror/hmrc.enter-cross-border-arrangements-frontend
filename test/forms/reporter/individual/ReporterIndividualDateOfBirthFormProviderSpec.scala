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

package forms.reporter.individual

import forms.behaviours.DateBehaviours
import helpers.DateHelper
import helpers.DateHelper.today
import play.api.data.FormError

import java.time.{LocalDate, ZoneOffset}

class ReporterIndividualDateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new ReporterIndividualDateOfBirthFormProvider()()

  ".dob" - {

    val fieldName = "dob"

    val validData = datesBetween(
      min = LocalDate.of(1903, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, fieldName, validData)

    behave like mandatoryDateField(form, fieldName, "reporterIndividualDateOfBirth.error.required.all")

    behave like dateFieldWithMax(
      form = form,
      key = fieldName,
      max = today,
      formError = FormError(
        fieldName, "reporterIndividualDateOfBirth.error.futureDate", Seq(DateHelper.formatDateToString(today))
      )
    )

    behave like dateFieldWithMin(
      form = form,
      key = fieldName,
      min = LocalDate.of(1903,1,1),
      formError = FormError(
        fieldName, "reporterIndividualDateOfBirth.error.pastDate"
      )
    )
  }
}
