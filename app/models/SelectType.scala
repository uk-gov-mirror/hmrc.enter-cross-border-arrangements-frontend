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

package models

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels._

sealed trait SelectType

object SelectType extends Enumerable.Implicits {

  case object Organisation extends WithName("organisation") with SelectType
  case object Individual extends WithName("individual") with SelectType

  val values: Seq[SelectType] = Seq(
    Organisation,
    Individual
  )

  def radios(form: Form[_])(implicit messages: Messages): Seq[Radios.Item] = {

    val field = form("confirm")
    val items = Seq(
      Radios.Radio(msg"selectType.organisation", Organisation.toString),
      Radios.Radio(msg"selectType.individual", Individual.toString)
    )

    Radios(field, items)
  }

  implicit val enumerable: Enumerable[SelectType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}