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
import forms.organisation.DoYouKnowAnyTINForUKOrganisationFormProvider
import helpers.JourneyHelpers.{currentIndexInsideLoop, getOrganisationName}
import models.Mode
import navigation.NavigatorForOrganisation
import pages.organisation.{DoYouKnowAnyTINForUKOrganisationPage, OrganisationLoopPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoYouKnowAnyTINForUKOrganisationController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            sessionRepository: SessionRepository,
                                                            navigator: NavigatorForOrganisation,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: DoYouKnowAnyTINForUKOrganisationFormProvider,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  private val form = formProvider()

  def onPageLoad(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(OrganisationLoopPage, id) match {
        case None => form
        case Some(value) if value.lift(index).isDefined =>
          val doYouKnowUTR = value.lift(index).get.doYouKnowUTR
          if (doYouKnowUTR.isDefined) {
            form.fill(doYouKnowUTR.get)
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
        "index" -> index
      )

      renderer.render("organisation/doYouKnowAnyTINForUKOrganisation.njk", json).map(Ok(_))
  }

  def redirect(id: Int, checkRoute: CheckRoute, value: Option[Boolean], index: Int = 0): Call =
    navigator.routeMap(DoYouKnowAnyTINForUKOrganisationPage)(checkRoute)(id)(value)(index)

  def onSubmit(id: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "id" -> id,
            "mode"   -> mode,
            "radios" -> Radios.yesNo(formWithErrors("confirm")),
            "organisationName" -> getOrganisationName(request.userAnswers, id),
            "index" -> index
          )

          renderer.render("organisation/doYouKnowAnyTINForUKOrganisation.njk", json).map(BadRequest(_))
        },
        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouKnowAnyTINForUKOrganisationPage, id, value))
            updatedAnswersWithLoopDetails <- Future.fromTry(updatedAnswers.set(OrganisationLoopPage, id, index)(_.copy(doYouKnowUTR = Some(value))))
            _                             <- sessionRepository.set(updatedAnswersWithLoopDetails)
            checkRoute                    =  toCheckRoute(mode, updatedAnswersWithLoopDetails, id)
          } yield Redirect(redirect(id, checkRoute, Some(value), currentIndexInsideLoop(request)))
        }
      )
  }
}
