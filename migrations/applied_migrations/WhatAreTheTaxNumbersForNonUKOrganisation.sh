#!/bin/bash

echo ""
echo "Applying migration WhatAreTheTaxNumbersForNonUKOrganisation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /whatAreTheTaxNumbersForNonUKOrganisation                        controllers.WhatAreTheTaxNumbersForNonUKOrganisationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /whatAreTheTaxNumbersForNonUKOrganisation                        controllers.WhatAreTheTaxNumbersForNonUKOrganisationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeWhatAreTheTaxNumbersForNonUKOrganisation                  controllers.WhatAreTheTaxNumbersForNonUKOrganisationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeWhatAreTheTaxNumbersForNonUKOrganisation                  controllers.WhatAreTheTaxNumbersForNonUKOrganisationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatAreTheTaxNumbersForNonUKOrganisation.title = whatAreTheTaxNumbersForNonUKOrganisation" >> ../conf/messages.en
echo "whatAreTheTaxNumbersForNonUKOrganisation.heading = whatAreTheTaxNumbersForNonUKOrganisation" >> ../conf/messages.en
echo "whatAreTheTaxNumbersForNonUKOrganisation.checkYourAnswersLabel = whatAreTheTaxNumbersForNonUKOrganisation" >> ../conf/messages.en
echo "whatAreTheTaxNumbersForNonUKOrganisation.error.required = Enter whatAreTheTaxNumbersForNonUKOrganisation" >> ../conf/messages.en
echo "whatAreTheTaxNumbersForNonUKOrganisation.error.length = WhatAreTheTaxNumbersForNonUKOrganisation must be 10 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhatAreTheTaxNumbersForNonUKOrganisationUserAnswersEntry: Arbitrary[(WhatAreTheTaxNumbersForNonUKOrganisationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[WhatAreTheTaxNumbersForNonUKOrganisationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhatAreTheTaxNumbersForNonUKOrganisationPage: Arbitrary[WhatAreTheTaxNumbersForNonUKOrganisationPage.type] =";\
    print "    Arbitrary(WhatAreTheTaxNumbersForNonUKOrganisationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(WhatAreTheTaxNumbersForNonUKOrganisationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def whatAreTheTaxNumbersForNonUKOrganisation: Option[Row] = userAnswers.get(WhatAreTheTaxNumbersForNonUKOrganisationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"whatAreTheTaxNumbersForNonUKOrganisation.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.WhatAreTheTaxNumbersForNonUKOrganisationController.onPageLoad(CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"whatAreTheTaxNumbersForNonUKOrganisation.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration WhatAreTheTaxNumbersForNonUKOrganisation completed"
