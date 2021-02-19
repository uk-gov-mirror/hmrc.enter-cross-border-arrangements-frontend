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
import connectors.{CrossBorderArrangementsConnector, HistoryConnector, ValidationConnector}
import controllers.actions._
import controllers.mixins.DefaultRouting
import helpers.TaskListHelper._
import models.disclosure.DisclosureType.Dac6rep
import models.hallmarks.JourneyStatus
import models.hallmarks.JourneyStatus.Completed
import models.{NormalMode, UserAnswers}
import models.{GeneratedIDs, NormalMode, UnsubmittedDisclosure, UserAnswers}
import navigation.NavigatorForDisclosure
import org.slf4j.LoggerFactory
import pages.affected.AffectedStatusPage
import pages.arrangement.ArrangementStatusPage
import pages.disclosure.{DisclosureDetailsPage, DisclosureStatusPage, FirstInitialDisclosureMAPage}
import pages.disclosure.{DisclosureDetailsPage, DisclosureStatusPage, DisclosureTypePage}
import pages.enterprises.AssociatedEnterpriseStatusPage
import pages.hallmarks.HallmarkStatusPage
import pages.intermediaries.IntermediariesStatusPage
import pages.reporter.ReporterStatusPage
import pages.taxpayer.{RelevantTaxpayerStatusPage, TaxpayerLoopPage}
import pages.unsubmitted.UnsubmittedDisclosurePage
import pages.{Dac6MetaDataPage, GeneratedIDPage, MessageRefIDPage, QuestionPage, ValidationErrorsPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{TransformationService, XMLGenerationService}
import uk.gov.hmrc.http.HeaderCarrier
import services.{EmailService, TransformationService, XMLGenerationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Radios.MessageInterpolators
import javax.inject.Inject
import models.disclosure.DisclosureType.{Dac6add, Dac6del, Dac6new, Dac6rep}
import models.requests.DataRequestWithContacts
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class DisclosureDetailsController @Inject()(
    override val messagesApi: MessagesApi,
    xmlGenerationService: XMLGenerationService,
    transformationService: TransformationService,
    emailService: EmailService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    contactRetrievalAction: ContactRetrievalAction,
    validationConnector: ValidationConnector,
    crossBorderArrangementsConnector: CrossBorderArrangementsConnector,
    historyConnector: HistoryConnector,
    frontendAppConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    navigator: NavigatorForDisclosure,
    renderer: Renderer,
    sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val logger = LoggerFactory.getLogger(getClass)

  def onPageLoad(id: Int): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val arrangementMessage: String = request.userAnswers.fold("") {
        value => value.get(DisclosureDetailsPage, id).flatMap(_.arrangementID)
          .map(msg"disclosureDetails.heading.forArrangement".withArgs(_).resolve)
          .getOrElse("")
      }

      val addedTaxpayer = request.userAnswers.flatMap(_.get(TaxpayerLoopPage, id)) match {
        case Some(taxpayer) if taxpayer.nonEmpty =>
          true
        case _ =>
          false
      }

      isReplacingAMarketableAddDisclosure(request.userAnswers.get, id).flatMap { replaceAMarketableAddDisclosure =>

        val json = Json.obj(
          "id" -> id,
          "arrangementID" -> arrangementMessage,
          "hallmarksTaskListItem" -> hallmarksItem(request.userAnswers.get, HallmarkStatusPage, id),
          "arrangementDetailsTaskListItem" -> arrangementsItem(request.userAnswers.get, ArrangementStatusPage, id),
          "reporterDetailsTaskListItem" -> reporterDetailsItem(request.userAnswers.get, ReporterStatusPage, id),
          "relevantTaxpayerTaskListItem" -> relevantTaxpayersItem(request.userAnswers.get, RelevantTaxpayerStatusPage, id),
          "associatedEnterpriseTaskListItem" -> associatedEnterpriseItem(request.userAnswers.get, AssociatedEnterpriseStatusPage, id),
          "intermediariesTaskListItem" -> intermediariesItem(request.userAnswers.get, IntermediariesStatusPage, id),
          "othersAffectedTaskListItem" -> othersAffectedItem(request.userAnswers.get, AffectedStatusPage, id),
          "disclosureTaskListItem" -> disclosureTypeItem(request.userAnswers.get, DisclosureStatusPage, id),
          "userCanSubmit" ->
            userCanSubmit(request.userAnswers.get, id,
              frontendAppConfig.affectedToggle, frontendAppConfig.associatedEnterpriseToggle, addedTaxpayer, replaceAMarketableAddDisclosure),
          "displaySectionOptional" -> displaySectionOptional(request.userAnswers.get, id, replaceAMarketableAddDisclosure),
          "backLink" -> backLink
        )
        renderer.render("disclosure/disclosureDetails.njk", json).map(Ok(_))
      }
  }


  def sendMail(ids: GeneratedIDs, id : Int)(implicit request: DataRequestWithContacts[_]): Future[Option[HttpResponse]] = {

    if (frontendAppConfig.associatedEnterpriseToggle && request.userAnswers.get(GeneratedIDPage, id).isDefined) {
      val generatedId = request.userAnswers.get(GeneratedIDPage, id).get

      // ANEW WANTS

      val generatedIDs =
        (ids.arrangementID, ids.disclosureID) match {
          case (Some(_), Some(_)) => ids  //DAC6NEW v
          case (_, Some(disclosureID)) => GeneratedIDs(generatedId.arrangementID, Some(disclosureID)) //DAC6ADD
          case _ => GeneratedIDs(generatedId.arrangementID, generatedId.disclosureID) //DAC6REP and DAC6DEL
        }

      val importInstruction =
        request.userAnswers.get(DisclosureTypePage, id) match {
          case Some(Dac6new) => "new"
          case Some(Dac6add) => "add"
          case Some(Dac6del) => "del"
          case Some(Dac6rep) => "rep"

        }

      emailService.sendEmail(request.contacts, generatedIDs, importInstruction, request.userAnswers.get(MessageRefIDPage, id))
    }
    else {
      logger.warn("Unable to send email")
      Future.successful(None)
    }
  }

  def onSubmit(id: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen contactRetrievalAction).async {
    implicit request =>
      //generate xml from user answers
      xmlGenerationService.createXmlSubmission(request.userAnswers, id).fold (
        error => {
          // TODO today we rely on task list enforcement to avoid incomplete xml to be submitted; we could add an extra layer of validation here
          logger.error("""Xml generation failed before validation: """.stripMargin, error)
          throw error
        },
        xml => {
          //send it off to be validated and business rules
          validationConnector.sendForValidation(xml).flatMap {
            _.fold(
              //did it fail? oh my god - hand back to the user to fix
              errors => {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ValidationErrorsPage, id, errors))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.confirmation.routes.DisclosureValidationErrorsController.onPageLoad(id).url)
              },

              //did it succeed - hand off to the backend to do it's generating thing
              messageRefId => {
                val uniqueXmlSubmission = transformationService.rewriteMessageRefID(xml, messageRefId)
                val submission = transformationService.constructSubmission("manual-submission.xml", request.enrolmentID, uniqueXmlSubmission)
                for {
                  ids                         <- crossBorderArrangementsConnector.submitXML(submission)
                  userAnswersWithIDs          <- Future.fromTry(request.userAnswers.set(GeneratedIDPage, id, ids))
                  updatedUserAnswersWithIDs   <- Future.fromTry(userAnswersWithIDs.set(MessageRefIDPage, id, messageRefId))
                  updatedUserAnswersWithFlags <- Future.fromTry(updateFlags(updatedUserAnswersWithIDs, id))
                  _                           <- sendMail(ids)
                  _                           <- sessionRepository.set(updatedUserAnswersWithFlags)
                } yield Redirect(controllers.confirmation.routes.FileTypeGatewayController.onRouting(id).url)
              }
            )
          }
        }
      )
  }

  private def isReplacingAMarketableAddDisclosure(userAnswers: UserAnswers, id: Int)
                                                 (implicit hc: HeaderCarrier): Future[Boolean] = {

    val disclosureDetails = userAnswers.get(DisclosureDetailsPage, id) match {
      case Some(details) => details
      case None => throw new Exception("Missing disclosure details")
    }

    if (disclosureDetails.disclosureType == Dac6rep) {
      historyConnector.retrieveFirstDisclosureForArrangementID(disclosureDetails.arrangementID.getOrElse("")).flatMap {
        firstDisclosureDetails =>
          historyConnector.searchDisclosures(disclosureDetails.disclosureID.getOrElse("")).flatMap {
            submissionHistory =>
              for {
                userAnswers <- Future.fromTry(userAnswers.setBase(FirstInitialDisclosureMAPage, firstDisclosureDetails.initialDisclosureMA))
                _           <- sessionRepository.set(userAnswers)
              } yield {
                if (submissionHistory.details.nonEmpty &&
                  submissionHistory.details.head.importInstruction == "Add" &&
                  firstDisclosureDetails.initialDisclosureMA) {
                  //Note: There should only be one submission returned with an ADD instruction for the given disclosure ID
                  true
                } else {
                  false
                }
              }
          }
      }
    } else {
      Future.successful(false)
    }
  }


  private[controllers] def updateFlags(userAnswers: UserAnswers, id: Int): Try[UserAnswers] = {
    (userAnswers.getBase(UnsubmittedDisclosurePage) map { unsubmittedDisclosures =>
      val unsubmittedDisclosure = UnsubmittedDisclosurePage.fromIndex(id)(userAnswers)
      val updatedUnsubmittedDisclosures = unsubmittedDisclosures.zipWithIndex.filterNot { _._2 == id }.map { _._1 }
      userAnswers.setBase(UnsubmittedDisclosurePage, updatedUnsubmittedDisclosures :+ unsubmittedDisclosure.copy(submitted = true))
    }).getOrElse(Failure(new IllegalArgumentException("Unable to update unsubmitted disclosure.")))
  }

  private def backLink: String =
    navigator.routeMap(DisclosureDetailsPage)(DefaultRouting(NormalMode))(None)(None)(0).url

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
    if (frontendAppConfig.associatedEnterpriseToggle) {
      ua.get(ReporterStatusPage, index) match {
        case Some(Completed) =>
          retrieveRowWithStatusBottomless(ua: UserAnswers,
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

    } else {
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
  }

  private def associatedEnterpriseItem(ua: UserAnswers,
                                       page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    (ua.get(TaxpayerLoopPage, index), ua.get(RelevantTaxpayerStatusPage, index))  match {
      case (Some(_), Some(Completed)) =>
        retrieveRowWithStatus(ua: UserAnswers,
          page,
          s"${frontendAppConfig.associatedEnterpriseUrl}/$index",
          linkContent = "disclosureDetails.associatedEnterpriseLink",
          id = "associatedEnterprise",
          ariaLabel = "connected-parties",
          index
        )

      case _ => taskListItemRestricted(
        "disclosureDetails.associatedEnterpriseLink", "connected-parties")
    }
  }

  private def othersAffectedItem(ua: UserAnswers,
                                 page: QuestionPage[JourneyStatus], index: Int)(implicit messages: Messages) = {

    retrieveRowWithStatus(ua: UserAnswers,
      page,
      s"${frontendAppConfig.othersAffectedUrl}/$index",
      linkContent = "disclosureDetails.othersAffectedLink",
      id = "othersAffected",
      ariaLabel = "connected-parties",
      index
    )
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
