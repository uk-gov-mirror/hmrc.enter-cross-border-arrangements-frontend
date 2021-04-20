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

package utils.model.rows

import models.CheckMode
import models.arrangement.ArrangementDetails
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._

trait ArrangementModelRows extends DisplayRowBuilder {

  def whatIsThisArrangementCalledPage(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey  = "whatIsThisArrangementCalled",
      content = formatMaxChars(arrangementDetails.arrangementName),
      href    = controllers.arrangement.routes.WhatIsThisArrangementCalledController.onPageLoad(id, CheckMode).url
    ))


  def whatIsTheImplementationDatePage(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey = "whatIsTheImplementationDate",
      content = Literal(arrangementDetails.implementationDate.format(dateFormatter)),
      href = controllers.arrangement.routes.WhatIsTheImplementationDateController.onPageLoad(id, CheckMode).url
    ))


  def buildWhyAreYouReportingThisArrangementNow(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    arrangementDetails.reportingReason map { answer =>
    toRow(
      msgKey = "whyAreYouReportingThisArrangementNow",
      content = msg"whyAreYouReportingThisArrangementNow.$answer",
      href = controllers.arrangement.routes.WhyAreYouReportingThisArrangementNowController.onPageLoad(id, CheckMode).url
    )
  }

  private def formatCountries(countries: List[String])(implicit messages: Messages): Html = {
    val list: String = if (countries.size > 1) {
      s"""<ul>
        |${countries.map(a => s"<li>${msg"whichExpectedInvolvedCountriesArrangement.$a".resolve}</li>").mkString("\n")}
        |</ul>""".stripMargin
    } else {
      countries.map(a => msg"whichExpectedInvolvedCountriesArrangement.$a".resolve).mkString
    }
    Html(list)
  }

  def whichExpectedInvolvedCountriesArrangement(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey  = "whichExpectedInvolvedCountriesArrangement",
      content = formatCountries(arrangementDetails.countriesInvolved),
      href    = controllers.arrangement.routes.WhichExpectedInvolvedCountriesArrangementController.onPageLoad(id, CheckMode).url
    ))

  def whatIsTheExpectedValueOfThisArrangement(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey  = "whatIsTheExpectedValueOfThisArrangement",
      content = lit"${arrangementDetails.expectedValue.currency} ${arrangementDetails.expectedValue.amount}",
      href    = controllers.arrangement.routes.WhatIsTheExpectedValueOfThisArrangementController.onPageLoad(id, CheckMode).url
    ))

  def whichNationalProvisionsIsThisArrangementBasedOn(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey  = "whichNationalProvisionsIsThisArrangementBasedOn",
      content = formatMaxChars(arrangementDetails.nationalProvisionDetails),
      href    = controllers.arrangement.routes.WhichNationalProvisionsIsThisArrangementBasedOnController.onPageLoad(id, CheckMode).url
    ))

  def giveDetailsOfThisArrangement(id: Int, arrangementDetails: ArrangementDetails)(implicit messages: Messages): Option[Row] =
    Some(toRow(
      msgKey  = "giveDetailsOfThisArrangement",
      content = formatMaxChars(arrangementDetails.arrangementDetails),
      href    = controllers.arrangement.routes.GiveDetailsOfThisArrangementController.onPageLoad(id, CheckMode).url
    ))

}