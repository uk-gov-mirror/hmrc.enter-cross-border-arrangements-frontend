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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import helpers.TaskListHelper._
import javax.inject.Inject
import models.UserAnswers
import models.hallmarks.JourneyStatus
import models.hallmarks.JourneyStatus.Completed
import pages.QuestionPage
import pages.arrangement.ArrangementStatusPage
import pages.disclosure.{DisclosureDetailsPage, DisclosureStatusPage}
import pages.hallmarks.HallmarkStatusPage
import pages.intermediaries.IntermediariesStatusPage
import pages.reporter.ReporterStatusPage
import pages.taxpayer.RelevantTaxpayerStatusPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Radios.MessageInterpolators

import scala.concurrent.ExecutionContext

class DisclosureDetailsController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    frontendAppConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(id: Int): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val arrangementMessage: String = request.userAnswers.fold("") {
        value => value.get(DisclosureDetailsPage, id).flatMap(_.arrangementID)
          .map(msg"disclosureDetails.heading.forArrangement".withArgs(_).resolve)
          .getOrElse("")
      }


      val json = Json.obj(
        "id"      -> id,
        "arrangementID" -> arrangementMessage,
        "hallmarksTaskListItem" -> hallmarksItem(request.userAnswers.get, HallmarkStatusPage, id),
        "arrangementDetailsTaskListItem" -> arrangementsItem(request.userAnswers.get, ArrangementStatusPage, id),
        "reporterDetailsTaskListItem" -> reporterDetailsItem(request.userAnswers.get, ReporterStatusPage, id),
        "relevantTaxpayerTaskListItem" -> relevantTaxpayersItem(request.userAnswers.get, RelevantTaxpayerStatusPage, id),
        "intermediariesTaskListItem" -> intermediariesItem(request.userAnswers.get, IntermediariesStatusPage, id),
        "disclosureTaskListItem" -> disclosureTypeItem(request.userAnswers.get, DisclosureStatusPage, id),
        "userCanSubmit" -> userCanSubmit(request.userAnswers.get, id),
        "displaySectionOptional" -> displaySectionOptional(request.userAnswers.get, id)
      )
      renderer.render("disclosureDetails.njk", json).map(Ok(_))
  }


  private def disclosureTypeItem(ua: UserAnswers,
                                 page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    ua.get(DisclosureStatusPage, index) match {
      case Some(Completed) =>
        taskListItemNotLinkedProvider(JourneyStatus.Completed.toString, "disclosureDetails.disclosureTypeLink", "disclosure", "disclosure-details")

      case _ =>
        retrieveRowWithStatus(ua,
          page,
          "",
          linkContent = "disclosureDetails.disclosureTypeLink",
          id = "disclosure",
          ariaLabel = "disclosure-details",
          index
        )
    }
  }

  private def hallmarksItem(ua: UserAnswers,
                            page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    val dynamicLink = startJourneyOrCya(ua, page, s"${frontendAppConfig.hallmarksUrl}/$index", s"${frontendAppConfig.hallmarksCYAUrl}/$index", index)

    retrieveRowWithStatus(ua: UserAnswers,
      page,
      dynamicLink,
      linkContent = "disclosureDetails.hallmarksLink",
      id = "hallmarks",
      ariaLabel = "arrangementDetails",
      index
    )
  }

  private def arrangementsItem(ua: UserAnswers,
                               page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    val dynamicLink = startJourneyOrCya(ua, page, s"${frontendAppConfig.arrangementsUrl}/$index", s"${frontendAppConfig.arrangementsCYAUrl}/$index", index)

    retrieveRowWithStatus(ua: UserAnswers,
      page,
      dynamicLink,
      linkContent = "disclosureDetails.arrangementDetailsLink",
      id = "arrangementDetails",
      ariaLabel = "arrangementDetails",
      index
    )
  }

  private def reporterDetailsItem(ua: UserAnswers,
                                  page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    val dynamicLink = startJourneyOrCya(ua, page, s"${frontendAppConfig.reportersUrl}/$index", s"${frontendAppConfig.reportersCYAUrl}/$index", index)

    retrieveRowWithStatus(ua: UserAnswers,
      page,
      dynamicLink,
      linkContent = "disclosureDetails.reporterDetailsLink",
      id = "reporter",
      ariaLabel = "reporterDetails",
      index
    )
  }

  private def relevantTaxpayersItem(ua: UserAnswers,
                                    page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    ua.get(ReporterStatusPage, index) match {
      case Some(Completed) =>
        retrieveRowWithStatus(ua: UserAnswers,
          page,
          s"${frontendAppConfig.taxpayersUrl}/$index",
          linkContent = "disclosureDetails.relevantTaxpayersLink",
          id = "taxpayers",
          ariaLabel = "connected-parties",
          index
        )

      case _ => taskListItemRestricted(
        "disclosureDetails.relevantTaxpayersLink", "connected-parties")
    }
  }

  private def intermediariesItem(ua: UserAnswers,
                                 page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    ua.get(ReporterStatusPage, index) match {
      case Some(Completed) =>
        retrieveRowWithStatus(ua: UserAnswers,
          page,
          s"${frontendAppConfig.intermediariesUrl}/$index",
          linkContent = "disclosureDetails.intermediariesLink",
          id = "intermediaries",
          ariaLabel = "connected-parties",
          index
        )

      case _ => taskListItemRestricted(
        "disclosureDetails.intermediariesLink", "connected-parties")
    }
  }
}
