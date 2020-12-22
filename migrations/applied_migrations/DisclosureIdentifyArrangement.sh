#!/bin/bash

echo ""
echo "Applying migration DisclosureIdentifyArrangement"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /disclosureIdentifyArrangement                        controllers.DisclosureIdentifyArrangementController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /disclosureIdentifyArrangement                        controllers.DisclosureIdentifyArrangementController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDisclosureIdentifyArrangement                  controllers.DisclosureIdentifyArrangementController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDisclosureIdentifyArrangement                  controllers.DisclosureIdentifyArrangementController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "disclosureIdentifyArrangement.title = disclosureIdentifyArrangement" >> ../conf/messages.en
echo "disclosureIdentifyArrangement.heading = disclosureIdentifyArrangement" >> ../conf/messages.en
echo "disclosureIdentifyArrangement.checkYourAnswersLabel = disclosureIdentifyArrangement" >> ../conf/messages.en
echo "disclosureIdentifyArrangement.error.required = Enter disclosureIdentifyArrangement" >> ../conf/messages.en
echo "disclosureIdentifyArrangement.error.length = DisclosureIdentifyArrangement must be 20 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDisclosureIdentifyArrangementUserAnswersEntry: Arbitrary[(DisclosureIdentifyArrangementPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DisclosureIdentifyArrangementPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDisclosureIdentifyArrangementPage: Arbitrary[DisclosureIdentifyArrangementPage.type] =";\
    print "    Arbitrary(DisclosureIdentifyArrangementPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DisclosureIdentifyArrangementPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def disclosureIdentifyArrangement: Option[Row] = userAnswers.get(DisclosureIdentifyArrangementPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"disclosureIdentifyArrangement.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.DisclosureIdentifyArrangementController.onPageLoad(CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"disclosureIdentifyArrangement.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration DisclosureIdentifyArrangement completed"
