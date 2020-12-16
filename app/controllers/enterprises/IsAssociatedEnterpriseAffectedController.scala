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

package controllers.enterprises

import controllers.actions._
import forms.enterprises.IsAssociatedEnterpriseAffectedFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.enterprises.IsAssociatedEnterpriseAffectedPage
import pages.individual.IndividualNamePage
import pages.organisation.OrganisationNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsAssociatedEnterpriseAffectedController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: IsAssociatedEnterpriseAffectedFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IsAssociatedEnterpriseAffectedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "mode"   -> mode,
        "associatedEnterprise" -> getName(request.userAnswers),
        "radios" -> Radios.yesNo(preparedForm("confirm"))
      )

      renderer.render("enterprises/isAssociatedEnterpriseAffected.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "mode"   -> mode,
            "associatedEnterprise" -> getName(request.userAnswers),
            "radios" -> Radios.yesNo(formWithErrors("confirm"))
          )

          renderer.render("enterprises/isAssociatedEnterpriseAffected.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IsAssociatedEnterpriseAffectedPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IsAssociatedEnterpriseAffectedPage, mode, updatedAnswers))
      )
  }

  private def getName(userAnswers: UserAnswers) = {
    (userAnswers.get(IndividualNamePage), userAnswers.get(OrganisationNamePage)) match {
      case (Some(name), _) => name.displayName
      case (_, Some(name)) => name
      case _ => "this associated enterprise"
    }
  }
}