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

package controllers.taxpayer

import controllers.actions._
import forms.taxpayer.TaxpayerSelectTypeFormProvider
import models.{Mode, NormalMode, SelectType, UserAnswersHelper}
import navigation.Navigator
import pages.taxpayer.{RelevantTaxpayerStatusPage, TaxpayerSelectTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import models.hallmarks.JourneyStatus

import scala.concurrent.{ExecutionContext, Future}

class TaxpayerSelectTypeController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TaxpayerSelectTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(TaxpayerSelectTypePage, id) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "id"      -> id,
        "mode"    -> mode,
        "radios"  -> SelectType.radios(preparedForm)
      )

      renderer.render("taxpayer/selectType.njk", json).map(Ok(_))
  }

  def onSubmit(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "id" -> id,
            "mode"   -> mode,
            "radios" -> SelectType.radios(formWithErrors)
          )

          renderer.render("taxpayer/selectType.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- UserAnswersHelper.updateUserAnswers(request.userAnswers, id, TaxpayerSelectTypePage, value)
            updatedAnswersWithStatus <- Future.fromTry(updatedAnswers.set(RelevantTaxpayerStatusPage, id, JourneyStatus.InProgress))
            redirectMode   =  if (request.userAnswers.hasNewValue(TaxpayerSelectTypePage, id, value)) NormalMode else mode
            _              <- sessionRepository.set(updatedAnswersWithStatus)
          } yield Redirect(navigator.nextPage(TaxpayerSelectTypePage, id, redirectMode, updatedAnswers))
      )
  }
}