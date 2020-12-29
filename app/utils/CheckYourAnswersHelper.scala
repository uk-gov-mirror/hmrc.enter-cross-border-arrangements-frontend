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

package utils

import models.hallmarks.HallmarkA._
import models.hallmarks.HallmarkC.C1
import models.hallmarks.HallmarkC1._
import models.hallmarks.HallmarkCategories.{CategoryA, CategoryB}
import models.hallmarks.HallmarkD.D1
import models.hallmarks.HallmarkD1.D1other
import models.{CheckMode, UserAnswers}
import pages.hallmarks._
import pages.intermediaries.WhatTypeofIntermediaryPage
import pages.organisation.{PostcodePage, SelectAddressPage}
import pages.reporter.individual.{ReporterIndividualDateOfBirthPage, ReporterIndividualNamePage, ReporterIndividualPlaceOfBirthPage, ReporterIsIndividualAddressUKPage}
import pages.taxpayer.TaxpayerSelectTypePage
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._
import utils.rows._

class CheckYourAnswersHelper(val userAnswers: UserAnswers)(implicit val messages: Messages)
  extends IndividualRows with OrganisationRows with ArrangementRows with EnterpriseRows with TaxpayerRows with DisclosureRows {

import pages.intermediaries.WhatTypeofIntermediaryPage

  def reporterIsIndividualAddressUK: Option[Row] = userAnswers.get(ReporterIsIndividualAddressUKPage) map {
    answer =>
      Row(
        key     = Key(msg"reporterIsIndividualAddressUK.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.reporter.individual.routes.ReporterIsIndividualAddressUKController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"reporterIsIndividualAddressUK.checkYourAnswersLabel"))
          )
        )
      )
  }

  def reporterIndividualPlaceOfBirth: Option[Row] = userAnswers.get(ReporterIndividualPlaceOfBirthPage) map {
    answer =>
      Row(
        key     = Key(msg"reporterIndividualPlaceOfBirth.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.reporter.individual.routes.ReporterIndividualPlaceOfBirthController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"reporterIndividualPlaceOfBirth.checkYourAnswersLabel"))
          )
        )
      )
  }

  def reporterIndividualDateOfBirth: Option[Row] = userAnswers.get(ReporterIndividualDateOfBirthPage) map {
    answer =>
      Row(
        key     = Key(msg"reporterIndividualDateOfBirth.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(Literal(answer.format(dateFormatter))),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.reporter.individual.routes.ReporterIndividualDateOfBirthController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"reporterIndividualDateOfBirth.checkYourAnswersLabel"))
          )
        )
      )
  }

  def reporterIndividualName: Option[Row] = userAnswers.get(ReporterIndividualNamePage) map {
    answer =>
      Row(
        key     = Key(msg"reporterIndividualName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"${answer.firstName} ${answer.lastName}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.reporter.individual.routes.ReporterIndividualNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"reporterIndividualName.checkYourAnswersLabel"))
          )
        )
      )
  }

  def whatTypeofIntermediary: Option[Row] = userAnswers.get(WhatTypeofIntermediaryPage) map {
    answer =>
      Row(
        key     = Key(msg"whatTypeofIntermediary.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(msg"whatTypeofIntermediary.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.intermediaries.routes.WhatTypeofIntermediaryController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"whatTypeofIntermediary.checkYourAnswersLabel"))
          )
        )
      )
  }

  def youHaveNotAddedAnyIntermediaries: Option[Row] = userAnswers.get(pages.intermediaries.YouHaveNotAddedAnyIntermediariesPage) map {
    answer =>
      toRow(
        msgKey  = "youHaveNotAddedAnyIntermediaries",
        content = msg"youHaveNotAddedAnyIntermediaries.$answer",
        href    = controllers.enterprises.routes.YouHaveNotAddedAnyAssociatedEnterprisesController.onPageLoad(CheckMode).url
      )
  }

  def selectType: Option[Row] = userAnswers.get(TaxpayerSelectTypePage) map {
    answer =>
      Row(
        key     = Key(msg"selectType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(msg"selectType.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.taxpayer.routes.TaxpayerSelectTypeController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"selectType.checkYourAnswersLabel"))
          )
        )
      )
  }

  def hallmarkD1Other: Option[Row] = userAnswers.get(HallmarkD1OtherPage) flatMap {
    answer => userAnswers.get(HallmarkD1Page) match {
      case Some(hallmarkSet) if hallmarkSet.contains(D1other) =>
        Some(Row(
          key = Key(msg"hallmarkD1Other.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
          value = Value(formatMaxChars(answer)),
          actions = List(
            Action(
              content = msg"site.edit",
              href = controllers.hallmarks.routes.HallmarkD1OtherController.onPageLoad(CheckMode).url,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"hallmarkD1Other.checkYourAnswersLabel"))
            )
          )
        ))
      case _ => None
    }
  }

  def mainBenefitPredicate[A](set: Option[Set[A]], elem: A): Boolean = {
    set match {
      case Some(hm) => hm.contains(elem)
      case None => false
    }
  }

  def mainBenefitTest: Option[Row] = if(
    mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1bi) ||
      mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1c) ||
      mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1d) ||
      mainBenefitPredicate(userAnswers.get(HallmarkCategoriesPage), CategoryA) ||
      mainBenefitPredicate(userAnswers.get(HallmarkCategoriesPage), CategoryB)) {

    userAnswers.get(MainBenefitTestPage) map {
      answer =>
        Row(
          key     = Key(msg"mainBenefitTest.checkYourAnswersLabel"),
          value   = Value(yesOrNo(answer)),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = controllers.hallmarks.routes.MainBenefitTestController.onPageLoad(CheckMode).url,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"mainBenefitTest.checkYourAnswersLabel"))
            )
          )
        )
    }
  } else{
    None
  }

  def hallmarkCategories: Option[Row] = userAnswers.get(HallmarkCategoriesPage) map {
    answer =>
      Row(
        key     = Key(msg"hallmarkCategories.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-third")),
        value   = Value(Html(answer.map(a => msg"hallmarkCategories.$a".resolve).mkString(",<br>"))),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = controllers.hallmarks.routes.HallmarkCategoriesController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"hallmarkCategories.checkYourAnswersLabel"))
          )
        )
      )
  }

  def buildHallmarksRow(ua: UserAnswers): Row = {

    val hallmarkCPage = ua.get(HallmarkCPage)  match {
      case Some(set) if set.contains(C1) && set.size == 1 => None
      case Some(set) if set.contains(C1) => Some(set.filter(_ != C1))
      case hallmarkSet => hallmarkSet
    }

    val hallmarkDPage = ua.get(HallmarkDPage)  match {
      case Some(set) if set.contains(D1) && set.size == 1 => None
      case Some(set) if set.contains(D1) => Some(set.filter(_ != D1))
      case hallmarkSet => hallmarkSet
    }

    val hallmarkPages = Seq(
      ua.get(HallmarkAPage),
      ua.get(HallmarkBPage),
      ua.get(HallmarkC1Page),
      ua.get(HallmarkD1Page),
      hallmarkCPage,
      hallmarkDPage,
      ua.get(HallmarkEPage)
    )

    val selectedHallmarkParts = hallmarkPages.collect{ case Some(value) => value }

    val hallmarksList = for {
      selectedHallmark <- selectedHallmarkParts
    } yield {
      selectedHallmark.map(_.toString).toList.sorted.map(hallmark => msg"$hallmark".resolve).mkString(", ")
    }

    Row(
      key     = Key(msg"checkYourAnswers.selectedHallmarks.label"),
      value   = Value(msg"${hallmarksList.mkString(", ")}"),
      actions = List(
        Action(
          content            = msg"site.edit",
          href               = controllers.hallmarks.routes.HallmarkCategoriesController.onPageLoad(CheckMode).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"checkYourAnswers.selectedHallmarks.label"))
        )
      )
    )

  }

  def buildTaxpayerDetails(taxpayerDetails: Seq[Row], countryDetails: Seq[Row]) : Seq[SummaryList.Row] = {
    Seq(
      taxpayerSelectType,
    ).flatten ++
      taxpayerDetails ++
      countryDetails ++
      Seq(whatIsTaxpayersStartDateForImplementingArrangement).flatten
  }
}
