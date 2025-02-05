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

import helpers.data.ValidUserAnswersForSubmission.{validEmail, validIndividual}
import pages.behaviours.PageBehaviours


class EmailAddressForIndividualPageSpec extends PageBehaviours {

  "EmailAddressForIndividualPage" - {

    beRetrievable[String](EmailAddressForIndividualPage)

    beSettable[String](EmailAddressForIndividualPage)

    beRemovable[String](EmailAddressForIndividualPage)
  }

  "can restore from model " - {

    "- when email exists " in {

      EmailAddressForIndividualPage.getFromModel(validIndividual) mustBe(Some(validEmail))
    }

    "- when email is empty " in {

      EmailAddressForIndividualPage.getFromModel(validIndividual.copy(emailAddress = None)) mustBe(None)
    }
  }
}
