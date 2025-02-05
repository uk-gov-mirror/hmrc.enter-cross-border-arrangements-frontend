#!/bin/bash

echo ""
echo "Applying migration RemoveTaxpayer"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /removeTaxpayer                        controllers.RemoveTaxpayerController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /removeTaxpayer                        controllers.RemoveTaxpayerController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeRemoveTaxpayer                  controllers.RemoveTaxpayerController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeRemoveTaxpayer                  controllers.RemoveTaxpayerController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "removeTaxpayer.title = removeTaxpayer" >> ../conf/messages.en
echo "removeTaxpayer.heading = removeTaxpayer" >> ../conf/messages.en
echo "removeTaxpayer.checkYourAnswersLabel = removeTaxpayer" >> ../conf/messages.en
echo "removeTaxpayer.error.required = Select yes if removeTaxpayer" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRemoveTaxpayerUserAnswersEntry: Arbitrary[(RemoveTaxpayerPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[RemoveTaxpayerPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRemoveTaxpayerPage: Arbitrary[RemoveTaxpayerPage.type] =";\
    print "    Arbitrary(RemoveTaxpayerPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(RemoveTaxpayerPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def removeTaxpayer: Option[Row] = userAnswers.get(RemoveTaxpayerPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"removeTaxpayer.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.RemoveTaxpayerController.onPageLoad(CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"removeTaxpayer.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration RemoveTaxpayer completed"
