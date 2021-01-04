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

package forms.reporter

import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Gen
import play.api.data.FormError

class ReporterTaxResidentCountryFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "reporterTaxResidentCountry.error.required"
  val countriesSeq: Seq[Country] = Seq(Country("valid", "GB", "United Kingdom"), Country("valid", "FR", "France"))

  val form = new ReporterTaxResidentCountryFormProvider()(countriesSeq)

  ".countrySelction" - {

    val fieldName = "countrySelction"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Seq("GB", "FR"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}