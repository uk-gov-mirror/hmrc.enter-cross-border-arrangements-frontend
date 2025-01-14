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

package navigation

import controllers.mixins.CheckRoute
import models.Mode
import pages.Page
import play.api.mvc.Call

abstract class AbstractNavigator {

  val routeMap:  Page => CheckRoute => Int => Option[Any] => Int => Call

  val routeAltMap: Page => CheckRoute => Int => Option[Any] => Int => Call = _ => _ => _ => _ => _ => Call("GET", "/")

  private[navigation] def jumpOrCheckYourAnswers(id: Int, jumpTo: Call, checkRoute: CheckRoute): Call

  val indexRoute: Call = controllers.routes.IndexController.onPageLoad()

}
