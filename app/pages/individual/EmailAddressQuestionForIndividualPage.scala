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

package pages.individual

import models.UserAnswers
import models.individual.Individual
import pages.DetailsPage
import play.api.libs.json.JsPath

import scala.util.Try

case object EmailAddressQuestionForIndividualPage extends DetailsPage[Boolean, Individual] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "emailAddressQuestionForIndividual"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers, id: Int): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(EmailAddressForIndividualPage, id)
      case _ =>  super.cleanup(value, userAnswers, id)
    }

  override def getFromModel(model: Individual): Option[Boolean] = model.emailAddress.map(_ => true).orElse(Some(false))
}
