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

package controllers.arrangement

import controllers.actions._
import forms.arrangement.WhatIsThisArrangementCalledFormProvider
import javax.inject.Inject
import models.Mode
import models.hallmarks.JourneyStatus
import navigation.Navigator
import pages.arrangement.{ArrangementStatusPage, WhatIsThisArrangementCalledPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class WhatIsThisArrangementCalledController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhatIsThisArrangementCalledFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhatIsThisArrangementCalledPage, id) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "id" -> id,
        "mode" -> mode
      )

      renderer.render("arrangement/whatIsThisArrangementCalled.njk", json).map(Ok(_))
  }

  def onSubmit(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form" -> formWithErrors,
            "id" -> id,
            "mode" -> mode
          )

          renderer.render("arrangement/whatIsThisArrangementCalled.njk", json).map(BadRequest(_))
        },
        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsThisArrangementCalledPage, id, value))
            updatedAnswersWithStatus <- Future.fromTry(updatedAnswers.set(ArrangementStatusPage, id, JourneyStatus.InProgress))
            _ <- sessionRepository.set(updatedAnswersWithStatus)

          } yield Redirect(navigator.nextPage(WhatIsThisArrangementCalledPage, id, mode, updatedAnswers))
        }
      )
  }
}
