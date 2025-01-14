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

package forms.arrangement

import forms.behaviours.{IntFieldBehaviours, StringFieldBehaviours}
import models.Currency
import org.scalacheck.Gen
import play.api.data.FormError

class WhatIsTheExpectedValueOfThisArrangementFormProviderSpec extends StringFieldBehaviours with IntFieldBehaviours {

  val form = new WhatIsTheExpectedValueOfThisArrangementFormProvider()(Seq(Currency("ALL", "LEK", "ALBANIA","Albanian Lek (ALL)")))

  ".currency" - {

    val fieldName = "currency"
    val requiredKey = "whatIsTheExpectedValueOfThisArrangement.error.currency.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Seq("AED", "ALL"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".amount" - {

    val fieldName = "amount"
    val requiredKey = "whatIsTheExpectedValueOfThisArrangement.error.amount.required"
    val wholeNumberKey=  "whatIsTheExpectedValueOfThisArrangement.error.amount.wholeNumber"
    val nonNumericKey =  "whatIsTheExpectedValueOfThisArrangement.error.amount.nonNumeric"

    behave like intField(
      form,
      fieldName,
      FormError(fieldName, nonNumericKey),
      FormError(fieldName,wholeNumberKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
