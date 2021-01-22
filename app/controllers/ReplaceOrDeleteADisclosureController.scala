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

import connectors.CrossBorderArrangementsConnector
import controllers.actions._
import forms.ReplaceOrDeleteADisclosureFormProvider

import javax.inject.Inject
import models.{Country, Mode}
import navigation.Navigator
import pages.ReplaceOrDeleteADisclosurePage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{Html, NunjucksSupport}
import utils.CountryListFactory

import scala.concurrent.{ExecutionContext, Future}

class ReplaceOrDeleteADisclosureController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    countryListFactory: CountryListFactory,
    crossBorderArrangementsConnector: CrossBorderArrangementsConnector,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ReplaceOrDeleteADisclosureFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val countries: Seq[Country] = countryListFactory.getCountryList().getOrElse(throw new Exception("Cannot retrieve country list"))
      val form = formProvider(countries, crossBorderArrangementsConnector)

      val preparedForm = request.userAnswers.get(ReplaceOrDeleteADisclosurePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "mode"   -> mode,
        "arrangementIDLabel" -> arrangementIDLabel
      )

      renderer.render("replaceOrDeleteADisclosure.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val countries: Seq[Country] = countryListFactory.getCountryList().getOrElse(throw new Exception("Cannot retrieve country list"))
      val form = formProvider(countries, crossBorderArrangementsConnector)

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "mode"   -> mode,
            "arrangementIDLabel" -> arrangementIDLabel
          )

          renderer.render("replaceOrDeleteADisclosure.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ReplaceOrDeleteADisclosurePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ReplaceOrDeleteADisclosurePage, mode, updatedAnswers))
      )
  }

  private def arrangementIDLabel()(implicit messages: Messages): Html = {
    Html(s"${{ messages("replaceOrDeleteADisclosure.arrangementID") }}" +
      s"<br><p class='govuk-body'>${{ messages("replaceOrDeleteADisclosure.arrangementID.p") }}</p>")
  }
}
