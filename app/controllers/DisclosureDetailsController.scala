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

import javax.inject.Inject
import pages.disclosure.DisclosureIdentifyArrangementPage
import play.api.i18n.{I18nSupport, MessagesApi}
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
        value => value.get(DisclosureIdentifyArrangementPage, id)
          .map(msg"disclosureDetails.heading.forArrangement".withArgs(_).resolve)
          .getOrElse("")
      }


      val json = Json.obj(
        "arrangementID" -> arrangementMessage,
        "hallmarksUrl" -> s"${frontendAppConfig.hallmarksUrl}/$id",
        "arrangementsUrl" -> s"${frontendAppConfig.arrangementsUrl}/$id",
        "reportersUrl" -> s"${frontendAppConfig.reportersUrl}/$id",
        "taxpayersUrl" -> s"${frontendAppConfig.taxpayersUrl}/$id",
        "intermediariesUrl" -> s"${frontendAppConfig.intermediariesUrl}/$id",
        "disclosureUrl" -> s"${frontendAppConfig.disclosureUrl}/$id"
      )


      renderer.render("disclosureDetails.njk", json).map(Ok(_))
  }

}
