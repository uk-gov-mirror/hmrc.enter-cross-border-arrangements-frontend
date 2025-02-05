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

package helpers

import models.UserAnswers
import models.disclosure.DisclosureType
import models.disclosure.DisclosureType.{Dac6add, Dac6rep}
import models.hallmarks.JourneyStatus
import models.hallmarks.JourneyStatus.{Completed, InProgress, NotStarted}
import models.reporter.RoleInArrangement
import pages.QuestionPage
import pages.affected.AffectedStatusPage
import pages.arrangement.ArrangementStatusPage
import pages.disclosure.{DisclosureDetailsPage, DisclosureStatusPage}
import pages.enterprises.AssociatedEnterpriseStatusPage
import pages.hallmarks.HallmarkStatusPage
import pages.intermediaries.IntermediariesStatusPage
import pages.reporter.{ReporterStatusPage, RoleInArrangementPage}
import pages.taxpayer.{RelevantTaxpayerStatusPage, TaxpayerLoopPage}
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.Html

object TaskListHelper  {

  val optionalCompletion: Seq[QuestionPage[JourneyStatus]] = Seq(HallmarkStatusPage, ArrangementStatusPage)

  def taskListItemRestricted(linkContent: String, ariaLabel: String, rowStyle: String)(implicit messages: Messages): Html = {
    Html(s"<li class='app-task-list__$rowStyle'><a class='app-task-list__task-name' aria-describedby='$ariaLabel'> ${messages(linkContent)}</a>" +
      s"<strong class='govuk-tag govuk-tag--grey app-task-list__task-completed' id='section-restricted'>${JourneyStatus.Restricted.toString}</strong> </li>")
  }

  def taskListItemLinkedProvider(url: String, status: String, linkContent: String, id: String, ariaLabel: String, rowStyle: String)(implicit messages: Messages): Html = {
    Html(s"<li class='app-task-list__$rowStyle'><a class='app-task-list__task-name' href='$url' aria-describedby='$ariaLabel'> ${messages(linkContent)}</a>" +
      s"<strong class='govuk-tag app-task-list__task-completed' id='$id'>$status</strong> </li>")
  }

  def taskListItemNotLinkedProvider(status: String, linkContent: String, id: String, ariaLabel: String, rowStyle: String)(implicit messages: Messages): Html = {
    Html(s"<li class='app-task-list__$rowStyle'><a class='app-task-list__task-name' aria-describedby='$ariaLabel'> ${messages(linkContent)}</a>" +
      s"<strong class='govuk-tag app-task-list__task-completed' id='$id'>$status</strong> </li>")
  }

  def retrieveRowWithStatus(ua: UserAnswers, page: QuestionPage[JourneyStatus],
                            url: String, linkContent: String, id: String, ariaLabel: String, rowStyle: String, index: Int)(implicit messages: Messages): Html = {
    ua.get(page, index) match {
      case Some(Completed) => taskListItemLinkedProvider(url, Completed.toString, linkContent, s"$id-completed", ariaLabel, rowStyle)
      case Some(InProgress) => taskListItemLinkedProvider(url, InProgress.toString, linkContent, s"$id-inProgress", ariaLabel, rowStyle)
      case _ => taskListItemLinkedProvider(url, NotStarted.toString, linkContent, s"$id-notStarted", ariaLabel, rowStyle)
    }
  }

  def haveAllJourneysBeenCompleted(pageList: Seq[_ <: QuestionPage[JourneyStatus]],
                                   ua: UserAnswers,
                                   id: Int,
                                   isInitialDisclosureMarketable: Boolean): Boolean = {
    pageList.map(page => ua.get(page, id) match {
      case Some(Completed) => true
      case Some(InProgress) if isInitialDisclosureMarketable && optionalCompletion.contains(page) => false
      case _ if isInitialDisclosureMarketable && optionalCompletion.contains(page) => true
      case _ => false
    }).forall(bool => bool)
  }

  def startJourneyOrCya(ua: UserAnswers, page: QuestionPage[JourneyStatus], url: String, altUrl: String, id: Int): String = {
    ua.get(page, id) match {
      case Some(Completed) => altUrl
      case _ => url
    }
  }

  private def getDisclosureTypeWithMAFlag (ua: UserAnswers, id: Int): (Option[DisclosureType], Boolean) = {
    val getMarketableFlag: Boolean = ua.get(DisclosureDetailsPage, id).exists(_.initialDisclosureMA)
    val getDisclosureType: Option[DisclosureType] = ua.get(DisclosureDetailsPage, id).map(_.disclosureType)

    (getDisclosureType, getMarketableFlag)
  }

  def displaySectionOptional(ua: UserAnswers, id: Int, isInitialDisclosureMarketable: Boolean)(implicit messages: Messages): String =  {
    getDisclosureTypeWithMAFlag(ua, id) match {
      case (Some(Dac6add), true) =>
        messages("disclosureDetails.optional")
      case (Some(Dac6rep), _) if isInitialDisclosureMarketable =>
        messages("disclosureDetails.optional")
      case _ =>
        ""
    }
  }

  def userCanSubmit(ua: UserAnswers,
                    id: Int,
                    isInitialDisclosureMarketable: Boolean): Boolean = {

    val submissionContainsTaxpayer: Boolean = ua.get(TaxpayerLoopPage, id).fold(false)(_.nonEmpty) ||
      ua.get(RoleInArrangementPage, id).fold(false)(_.equals(RoleInArrangement.Taxpayer))

    val mandatoryCompletion =
      if (submissionContainsTaxpayer) {
        Seq(ReporterStatusPage, RelevantTaxpayerStatusPage, IntermediariesStatusPage, DisclosureStatusPage, AffectedStatusPage, AssociatedEnterpriseStatusPage)
      } else {
        Seq(ReporterStatusPage, RelevantTaxpayerStatusPage, IntermediariesStatusPage, DisclosureStatusPage, AffectedStatusPage)
      }

    val listToCheckForCompletion: Seq[QuestionPage[JourneyStatus]] =
      getDisclosureTypeWithMAFlag(ua, id) match {
        case (Some(DisclosureType.Dac6add), true) if !isInitialDisclosureMarketable =>
          mandatoryCompletion
        case _ =>
          mandatoryCompletion ++ optionalCompletion
      }

    (ua.get(HallmarkStatusPage, id), ua.get(ArrangementStatusPage, id)) match {
      case (Some(Completed), None) if isInitialDisclosureMarketable => false
      case _ => haveAllJourneysBeenCompleted(listToCheckForCompletion, ua, id, isInitialDisclosureMarketable)
    }
  }
}

