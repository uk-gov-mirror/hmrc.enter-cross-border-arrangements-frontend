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

package pages.organisation

import helpers.data.ValidUserAnswersForSubmission.{validAddress, validOrganisation}
import models.Address
import pages.behaviours.PageBehaviours


class OrganisationAddressPageSpec extends PageBehaviours {

  "OrganisationAddressPage" - {

    beRetrievable[Address](OrganisationAddressPage)

    beSettable[Address](OrganisationAddressPage)

    beRemovable[Address](OrganisationAddressPage)
  }

  "can restore from model " - {

    "- when address exists " in {

      OrganisationAddressPage.getFromModel(validOrganisation) mustBe(Some(validAddress))
    }

    "- when address is empty " in {

      OrganisationAddressPage.getFromModel(validOrganisation.copy(address = None)) mustBe(None)
    }
  }
}
