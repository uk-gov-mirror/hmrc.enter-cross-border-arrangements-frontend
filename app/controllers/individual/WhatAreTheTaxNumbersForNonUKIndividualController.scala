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

package controllers.individual

import controllers.actions._
import controllers.mixins.{CheckRoute, RoutingSupport}
import forms.individual.WhatAreTheTaxNumbersForNonUKIndividualFormProvider
import helpers.JourneyHelpers.{currentIndexInsideLoop, getIndividualName}
import models.{Mode, TaxReferenceNumbers, UserAnswers}
import navigation.NavigatorForIndividual
import pages.individual.{IndividualLoopPage, WhatAreTheTaxNumbersForNonUKIndividualPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatAreTheTaxNumbersForNonUKIndividualController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NavigatorForIndividual,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhatAreTheTaxNumbersForNonUKIndividualFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  private val form = formProvider()

  def onPageLoad(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualLoopPage, id) match {
        case None => form
        case Some(value) if value.lift(index).isDefined =>
          val taxNumbers = value.lift(index).get.taxNumbersNonUK
          if (taxNumbers.isDefined) {
            form.fill(taxNumbers.get)
          } else {
            form
          }
        case Some(_) => form
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "id" -> id,
        "mode" -> mode,
        "individualName" -> getIndividualName(request.userAnswers, id),
        "country" -> getCountry(request.userAnswers, id),
        "index" -> index
      )

      renderer.render("individual/whatAreTheTaxNumbersForNonUKIndividual.njk", json).map(Ok(_))
  }

  def redirect(id: Int, checkRoute: CheckRoute, value: Option[TaxReferenceNumbers], index: Int): Call =
    navigator.routeMap(WhatAreTheTaxNumbersForNonUKIndividualPage)(checkRoute)(id)(value)(index)

  def onSubmit(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form" -> formWithErrors,
            "id" -> id,
            "mode" -> mode,
            "individualName" -> getIndividualName(request.userAnswers, id),
            "country" -> getCountry(request.userAnswers, id),
            "index" -> index
          )

          renderer.render("individual/whatAreTheTaxNumbersForNonUKIndividual.njk", json).map(BadRequest(_))
        },
        value => {

          for {
            updatedAnswers                <- Future.fromTry(request.userAnswers.set(WhatAreTheTaxNumbersForNonUKIndividualPage, id, value))
            updatedAnswersWithLoopDetails <- Future.fromTry(updatedAnswers.set(IndividualLoopPage, id, index)(_.copy(taxNumbersNonUK = Some(value))))
            _                             <- sessionRepository.set(updatedAnswersWithLoopDetails)
            checkRoute                    =  toCheckRoute(mode, updatedAnswersWithLoopDetails, id)
          } yield Redirect(redirect(id, checkRoute, Some(value), index))
        }
      )
  }

  private def getCountry(userAnswers: UserAnswers, id: Int)(implicit request: Request[AnyContent]): String = {
    userAnswers.get(IndividualLoopPage, id) match {
      case Some(loopDetailsSeq) =>
        val whichCountry = loopDetailsSeq(currentIndexInsideLoop(request)).whichCountry
        if (whichCountry.isDefined) {
          whichCountry.get.description
        } else {
          "the country"
        }
      case None => "the country"
    }
  }
}
