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

package helpers.xml
import models.Submission
import models.enterprises.AssociatedEnterprise

import scala.xml.NodeSeq

case class AssociatedEnterprisesXMLSection(submission: Submission) {

  val associatedEnterprises: IndexedSeq[AssociatedEnterprise] = submission.associatedEnterprises

  private[xml] def buildAssociatedEnterprise(associatedEnterprise: AssociatedEnterprise): NodeSeq = {
    val optionalAffectedPerson = <AffectedPerson>{associatedEnterprise.isAffectedBy}</AffectedPerson>

    if (associatedEnterprise.individual.isDefined) {
      <AssociatedEnterprise>
        {IndividualXMLSection.buildIDForIndividual(associatedEnterprise.individual.get, isAssociatedEnterprise = true)}
        {optionalAffectedPerson}
      </AssociatedEnterprise>
    } else {
      <AssociatedEnterprise>
        {OrganisationXMLSection.buildIDForOrganisation(associatedEnterprise.organisation.get, isAssociatedEnterprise = true)}
        {optionalAffectedPerson}
      </AssociatedEnterprise>
    }
  }

  def buildAssociatedEnterprises(taxpayerName: String): NodeSeq = {
        val associatedEnterprisesList =
          associatedEnterprises.flatMap {
            associatedEnterprise =>
              if (associatedEnterprise.associatedTaxpayers.contains(taxpayerName)) {
                buildAssociatedEnterprise(associatedEnterprise)
              } else {
                NodeSeq.Empty
              }
          }

        if (associatedEnterprisesList.nonEmpty) {
          <AssociatedEnterprises>{associatedEnterprisesList}</AssociatedEnterprises>
        } else {
          NodeSeq.Empty
        }
  }

}