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

package controllers.hallmarks

import controllers.actions._
import javax.inject.Inject
import models.NormalMode
import models.hallmarks.HallmarkCategories.{CategoryD, CategoryE}
import pages.hallmarks.HallmarkCategoriesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class MainBenefitProblemController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(id: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(HallmarkCategoriesPage, id) match {
        case None => Future.successful(Redirect(routes.HallmarkCategoriesController.onPageLoad(id, NormalMode)))
        case Some(hallmarkCategories) =>

        val json = Json.obj(
          "hallmarkCategoryPageLink" -> routes.HallmarkCategoriesController.onPageLoad(id, NormalMode).url,
          "mainBenefitTestPageLink" -> routes.MainBenefitTestController.onPageLoad(id, NormalMode).url,
          "hallmarkSet"-> hallmarkCategories.diff(Set(CategoryD, CategoryE)).toSeq.sorted
        )

      renderer.render("hallmarks/mainBenefitProblem.njk", json).map(Ok(_))
    }
  }
}
