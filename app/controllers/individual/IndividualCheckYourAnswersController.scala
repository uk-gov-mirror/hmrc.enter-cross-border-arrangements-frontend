/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.individual

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.enterprises.AssociatedEnterpriseTypePage
import pages.taxpayer.TaxpayerSelectTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.CheckYourAnswersHelper

import scala.concurrent.{ExecutionContext, Future}

class IndividualCheckYourAnswersController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val associatedEnterpriseJourney: Boolean = request.userAnswers.get(AssociatedEnterpriseTypePage) match {
        case Some(_) => true
        case None => false
      }

      val relevantTaxpayerJourney: Boolean = request.userAnswers.get(TaxpayerSelectTypePage) match {
        case Some(_) => true
        case None => false
      }

      //TODO Below redirect is temporary until a solution about change routing is found
      if (associatedEnterpriseJourney) {
        Future.successful(Redirect(controllers.enterprises.routes.AssociatedEnterpriseCheckYourAnswersController.onPageLoad()))
      } else if(relevantTaxpayerJourney) {
        Future.successful(Redirect(controllers.taxpayer.routes.CheckYourAnswersTaxpayersController.onPageLoad()))
      }
      else {
        val helper = new CheckYourAnswersHelper(request.userAnswers)

        val individualSummary: Seq[SummaryList.Row] =
          Seq(helper.individualName, helper.individualDateOfBirth).flatten ++
            helper.buildIndividualPlaceOfBirthGroup ++
            helper.buildIndividualAddressGroup ++
            helper.buildIndividualEmailAddressGroup

        val countryDetails: Seq[SummaryList.Row] =
          helper.buildTaxResidencySummaryForIndividuals

        renderer.render(
          "individual/check-your-answers.njk",
          Json.obj("individualSummary" -> individualSummary,
            "countrySummary" -> countryDetails)
        ).map(Ok(_))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

    (request.userAnswers.get(IndividualNamePage), request.userAnswers.get(IndividualDateOfBirthPage)) match {
      case (Some(name), Some(dob)) =>
        val taxpayerLoopList = request.userAnswers.get(TaxpayerLoopPage) match {
          case Some(list) => // append to existing list
            list :+ Taxpayer.apply(Individual(name, dob))
          case None => // start new list
            IndexedSeq[Taxpayer](Taxpayer.apply(Individual(name, dob)))
        }
        for {
          userAnswersWithTaxpayerLoop <- Future.fromTry(request.userAnswers.set(TaxpayerLoopPage, taxpayerLoopList))
          _ <- sessionRepository.set(userAnswersWithTaxpayerLoop)
        } yield {
          Redirect(navigator.nextPage(CheckYourAnswersIndividualPage, mode, userAnswersWithTaxpayerLoop))
        }
      case _ => errorHandler.onServerError(request, throw new Exception("Error submitting - missing required individual taxpayer details"))
    }
  }
}
