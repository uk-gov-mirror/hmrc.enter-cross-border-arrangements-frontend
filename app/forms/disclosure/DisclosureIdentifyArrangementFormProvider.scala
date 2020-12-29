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

package forms.disclosure

import connectors.CrossBorderArrangementsConnector
import forms.mappings.Mappings
import models.Country
import play.api.data.Form
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DisclosureIdentifyArrangementFormProvider @Inject() extends Mappings {

  val startOfUKIDRegex = "^[GB]{2}.*"

  def apply(countryList: Seq[Country],
            crossBorderArrangementsConnector: CrossBorderArrangementsConnector
           )(implicit hc: HeaderCarrier): Form[String] =
    Form(
      "arrangementID" -> validatedArrangementIDText(
        "disclosureIdentifyArrangement.error.required",
        "disclosureIdentifyArrangement.error.invalid",
        countryList)
        .verifying("disclosureIdentifyArrangement.error.notFound",
          id => {
            if (id.toUpperCase.matches(startOfUKIDRegex)) {
              val verifyID = crossBorderArrangementsConnector.verifyArrangementId(id.toUpperCase)

              Await.result(verifyID, 5 seconds)
            } else {
              true
            }
          }
        )
    )
}