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

package controllers.organisation

import controllers.actions._
import controllers.mixins.{CheckRoute, RoutingSupport}
import forms.organisation.DoYouKnowTINForNonUKOrganisationFormProvider
import helpers.JourneyHelpers.{currentIndexInsideLoop, getOrganisationName}
import models.{LoopDetails, Mode, UserAnswers}
import navigation.NavigatorForOrganisation
import pages.organisation.{DoYouKnowTINForNonUKOrganisationPage, OrganisationLoopPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoYouKnowTINForNonUKOrganisationController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NavigatorForOrganisation,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: DoYouKnowTINForNonUKOrganisationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  def onPageLoad(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val country = getCountry(request.userAnswers, id)
      val form = formProvider(country)

      val preparedForm = request.userAnswers.get(OrganisationLoopPage, id) match {
        case None => form
        case Some(value) if value.lift(index).isDefined =>
          val doYouKnowTIN = value.lift(index).get.doYouKnowTIN
          if (doYouKnowTIN.isDefined) {
            form.fill(doYouKnowTIN.get)
          } else {
            form
          }
        case Some(_) => form
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "id" -> id,
        "mode"   -> mode,
        "radios" -> Radios.yesNo(preparedForm("confirm")),
        "organisationName" -> getOrganisationName(request.userAnswers, id),
        "country" -> country,
        "index" -> index
      )

      renderer.render("organisation/doYouKnowTINForNonUKOrganisation.njk", json).map(Ok(_))
  }

  def redirect(id: Int, checkRoute: CheckRoute, value: Option[Boolean], index: Int = 0): Call =
    navigator.routeMap(DoYouKnowTINForNonUKOrganisationPage)(checkRoute)(id)(value)(index)

  def onSubmit(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val country = getCountry(request.userAnswers, id)
      val form = formProvider(country)

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "id" -> id,
            "mode"   -> mode,
            "radios" -> Radios.yesNo(formWithErrors("confirm")),
            "organisationName" -> getOrganisationName(request.userAnswers, id),
            "country" -> country,
            "index" -> index
          )

          renderer.render("organisation/doYouKnowTINForNonUKOrganisation.njk", json).map(BadRequest(_))
        },
        value => {

          for {
            updatedAnswers                <- Future.fromTry(request.userAnswers.set(DoYouKnowTINForNonUKOrganisationPage, id, value))
            updatedAnswersWithLoopDetails <- Future.fromTry(updatedAnswers.set(OrganisationLoopPage, id, index)(_.copy(doYouKnowTIN = Some(value))))
            _                             <- sessionRepository.set(updatedAnswersWithLoopDetails)
            checkRoute                    =  toCheckRoute(mode, updatedAnswersWithLoopDetails, id)
          } yield Redirect(redirect(id, checkRoute, Some(value), currentIndexInsideLoop(request)))
        }
      )
  }

  private def getCountry(userAnswers: UserAnswers, id: Int)(implicit request: Request[AnyContent]): String = {
    userAnswers.get(OrganisationLoopPage, id) match {
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
