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

package utils.controllers

import controllers.routes
import models.{Mode, UserAnswers}
import play.api.mvc.Call
import repositories.SessionRepository

import scala.concurrent.ExecutionContext
import scala.util.Try

trait PageSubmitMixIn[A] {

  val sessionRepository: SessionRepository

  implicit val ec: ExecutionContext

  val setValue: UserAnswers => A => Try[UserAnswers]

  val failOnSubmit: Call = routes.SessionExpiredController.onPageLoad()

  def redirect(mode: Mode, value: Option[A], index: Int = 0, alternative: Boolean = false): Call

}
