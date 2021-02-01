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

package utils.rows

import models.CheckMode
import pages.enterprises.{AssociatedEnterpriseTypePage, IsAssociatedEnterpriseAffectedPage, SelectAnyTaxpayersThisEnterpriseIsAssociatedWithPage, YouHaveNotAddedAnyAssociatedEnterprisesPage}
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels._

trait EnterpriseRows extends RowBuilder {

  def youHaveNotAddedAnyAssociatedEnterprises(id: Int): Option[Row] = userAnswers.get(YouHaveNotAddedAnyAssociatedEnterprisesPage, id) map { answer =>

    toRow(
      msgKey  = "youHaveNotAddedAnyAssociatedEnterprises",
      content = msg"youHaveNotAddedAnyAssociatedEnterprises.$answer",
      href    = controllers.enterprises.routes.YouHaveNotAddedAnyAssociatedEnterprisesController.onPageLoad(id, CheckMode).url
    )
  }

  def selectAnyTaxpayersThisEnterpriseIsAssociatedWith(id: Int): Option[Row] = userAnswers.get(SelectAnyTaxpayersThisEnterpriseIsAssociatedWithPage, id) map {
    answer =>
      val taxpayers = if (answer.size > 1) {
        s"""<ul class="govuk-list govuk-list--bullet">
           |${answer.map(taxpayer => s"<li>$taxpayer</li>").mkString("\n")}
           |</ul>""".stripMargin
      } else {
        s"${answer.head}"
      }

      toRow(
        msgKey  = "selectAnyTaxpayersThisEnterpriseIsAssociatedWith",
        content = Html(s"$taxpayers"),
        href    = controllers.enterprises.routes.SelectAnyTaxpayersThisEnterpriseIsAssociatedWithController.onPageLoad(id, CheckMode).url
      )
  }

  def associatedEnterpriseType(id: Int): Option[Row] = userAnswers.get(AssociatedEnterpriseTypePage, id) map {
    answer =>
      toRow(
        msgKey = "associatedEnterpriseType",
        content = msg"selectType.$answer",
        href = controllers.enterprises.routes.AssociatedEnterpriseTypeController.onPageLoad(id, CheckMode).url
      )
  }

  def isAssociatedEnterpriseAffected(id: Int): Option[Row] = userAnswers.get(IsAssociatedEnterpriseAffectedPage, id) map {
    answer =>
      toRow(
        msgKey = "isAssociatedEnterpriseAffected",
        content = yesOrNo(answer),
        href = controllers.enterprises.routes.IsAssociatedEnterpriseAffectedController.onPageLoad(id, CheckMode).url
      )
  }

}
