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

package models

import models.taxpayer.TaxResidency
import play.api.libs.json.{Json, OFormat}

case class LoopDetails(taxResidentOtherCountries: Option[Boolean],
                       whichCountry: Option[Country],
                       doYouKnowTIN: Option[Boolean],
                       taxNumbersNonUK: Option[TaxReferenceNumbers],
                       doYouKnowUTR: Option[Boolean],
                       taxNumbersUK: Option[TaxReferenceNumbers]) {

  val matchingTINS: Option[TaxReferenceNumbers] =
    (whichCountry.exists(_.isUK), doYouKnowUTR.contains(true), doYouKnowTIN.contains(true)) match {
      case (true, true, _)  => taxNumbersUK
      case (false, _, true) => taxNumbersNonUK
      case _                => None
    }
}


object LoopDetails {

  def apply(): LoopDetails = this(None, None, None, None, None, None)

  def apply(taxResidency: TaxResidency): LoopDetails = if (taxResidency.isUK) {
    this(Some(false), taxResidency.country, None, None, taxResidency.hasNumbers, taxResidency.taxReferenceNumbers)
  } else {
    this(Some(false), taxResidency.country, taxResidency.hasNumbers, taxResidency.taxReferenceNumbers, None, None)
  }

  implicit val format: OFormat[LoopDetails] = Json.format[LoopDetails]
}

