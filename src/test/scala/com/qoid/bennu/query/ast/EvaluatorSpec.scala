package com.qoid.bennu.query.ast

import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.StandingQueryAction
import com.qoid.bennu.query.ast.Evaluator._
import net.model3.chrono._
import org.specs2.mutable.Specification

class EvaluatorSpec extends Specification {
  section("unit")

  "Evaluator =" should {

    "match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s = 'Test'"), TestRow()) must_== VTrue
    }

    "not match not equal string" in {
      Evaluator.evaluateQuery(Query.parse("s = 'Test2'"), TestRow()) must_== VFalse
    }

    "match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i = 6"), TestRow()) must_== VTrue
    }

    "not match not equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i = 5"), TestRow()) must_== VFalse
    }

    "match equal boolean" in {
      Evaluator.evaluateQuery(Query.parse("b = true"), TestRow()) must_== VTrue
    }

    "not match not equal boolean" in {
      Evaluator.evaluateQuery(Query.parse("b = false"), TestRow()) must_== VFalse
    }

    "match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d = '2014-06-03 11:04:00.000'"), TestRow()) must_== VTrue
    }

    "not match not equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d = '2014-06-03 11:04:01.000'"), TestRow()) must_== VFalse
    }

    "match equal InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid = 'abc123'"), TestRow()) must_== VTrue
    }

    "not match not equal InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid = 'abc1234'"), TestRow()) must_== VFalse
    }

    "match equal StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action = 'insert'"), TestRow()) must_== VTrue
    }

    "not match not equal StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action = 'update'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator <>" should {

    "match not equal string" in {
      Evaluator.evaluateQuery(Query.parse("s <> 'Test2'"), TestRow()) must_== VTrue
    }

    "not match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s <> 'Test'"), TestRow()) must_== VFalse
    }

    "match not equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i <> 5"), TestRow()) must_== VTrue
    }

    "not match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i <> 6"), TestRow()) must_== VFalse
    }

    "match not equal boolean" in {
      Evaluator.evaluateQuery(Query.parse("b <> false"), TestRow()) must_== VTrue
    }

    "not match equal boolean" in {
      Evaluator.evaluateQuery(Query.parse("b <> true"), TestRow()) must_== VFalse
    }

    "match not equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d <> '2014-06-03 11:04:01.000'"), TestRow()) must_== VTrue
    }

    "not match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d <> '2014-06-03 11:04:00.000'"), TestRow()) must_== VFalse
    }

    "match not equal InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid <> 'abc1234'"), TestRow()) must_== VTrue
    }

    "not match equal InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid <> 'abc123'"), TestRow()) must_== VFalse
    }

    "match not equal StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action <> 'update'"), TestRow()) must_== VTrue
    }

    "not match equal StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action <> 'insert'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator <" should {

    "match less than string" in {
      Evaluator.evaluateQuery(Query.parse("s < 'Tesu'"), TestRow()) must_== VTrue
    }

    "not match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s < 'Test'"), TestRow()) must_== VFalse
    }

    "not match greater than string" in {
      Evaluator.evaluateQuery(Query.parse("s < 'Tess'"), TestRow()) must_== VFalse
    }

    "match less than integer" in {
      Evaluator.evaluateQuery(Query.parse("i < 7"), TestRow()) must_== VTrue
    }

    "not match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i < 6"), TestRow()) must_== VFalse
    }

    "not match greater than integer" in {
      Evaluator.evaluateQuery(Query.parse("i < 5"), TestRow()) must_== VFalse
    }

    "match less than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d < '2014-06-03 11:04:01.000'"), TestRow()) must_== VTrue
    }

    "not match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d < '2014-06-03 11:04:00.000'"), TestRow()) must_== VFalse
    }

    "not match greater than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d < '2014-06-03 11:03:59.000'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator <=" should {

    "match less than string" in {
      Evaluator.evaluateQuery(Query.parse("s <= 'Tesu'"), TestRow()) must_== VTrue
    }

    "match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s <= 'Test'"), TestRow()) must_== VTrue
    }

    "not match greater than string" in {
      Evaluator.evaluateQuery(Query.parse("s <= 'Tess'"), TestRow()) must_== VFalse
    }

    "match less than integer" in {
      Evaluator.evaluateQuery(Query.parse("i <= 7"), TestRow()) must_== VTrue
    }

    "match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i <= 6"), TestRow()) must_== VTrue
    }

    "not match greater than integer" in {
      Evaluator.evaluateQuery(Query.parse("i <= 5"), TestRow()) must_== VFalse
    }

    "match less than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d <= '2014-06-03 11:04:01.000'"), TestRow()) must_== VTrue
    }

    "match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d <= '2014-06-03 11:04:00.000'"), TestRow()) must_== VTrue
    }

    "not match greater than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d <= '2014-06-03 11:03:59.000'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator >" should {

    "match greater than string" in {
      Evaluator.evaluateQuery(Query.parse("s > 'Tess'"), TestRow()) must_== VTrue
    }

    "not match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s > 'Test'"), TestRow()) must_== VFalse
    }

    "not match less than string" in {
      Evaluator.evaluateQuery(Query.parse("s > 'Tesu'"), TestRow()) must_== VFalse
    }

    "match greater than integer" in {
      Evaluator.evaluateQuery(Query.parse("i > 5"), TestRow()) must_== VTrue
    }

    "not match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i > 6"), TestRow()) must_== VFalse
    }

    "not match less than integer" in {
      Evaluator.evaluateQuery(Query.parse("i > 7"), TestRow()) must_== VFalse
    }

    "match greater than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d > '2014-06-03 11:03:59.000'"), TestRow()) must_== VTrue
    }

    "not match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d > '2014-06-03 11:04:00.000'"), TestRow()) must_== VFalse
    }

    "not match less than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d > '2014-06-03 11:04:01.000'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator >=" should {

    "match greater than string" in {
      Evaluator.evaluateQuery(Query.parse("s >= 'Tess'"), TestRow()) must_== VTrue
    }

    "match equal string" in {
      Evaluator.evaluateQuery(Query.parse("s >= 'Test'"), TestRow()) must_== VTrue
    }

    "not match less than string" in {
      Evaluator.evaluateQuery(Query.parse("s >= 'Tesu'"), TestRow()) must_== VFalse
    }

    "match greater than integer" in {
      Evaluator.evaluateQuery(Query.parse("i >= 5"), TestRow()) must_== VTrue
    }

    "match equal integer" in {
      Evaluator.evaluateQuery(Query.parse("i >= 6"), TestRow()) must_== VTrue
    }

    "not match less than integer" in {
      Evaluator.evaluateQuery(Query.parse("i >= 7"), TestRow()) must_== VFalse
    }

    "match greater than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d >= '2014-06-03 11:03:59.000'"), TestRow()) must_== VTrue
    }

    "match equal DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d >= '2014-06-03 11:04:00.000'"), TestRow()) must_== VTrue
    }

    "not match less than DateTime" in {
      Evaluator.evaluateQuery(Query.parse("d >= '2014-06-03 11:04:01.000'"), TestRow()) must_== VFalse
    }

  }

  "Evaluator +-*/" should {

    "integer plus" in {
      Evaluator.evaluateQuery(Query.parse("i + 2 = 8"), TestRow()) must_== VTrue
    }

    "integer minus" in {
      Evaluator.evaluateQuery(Query.parse("i - 2 = 4"), TestRow()) must_== VTrue
    }

    "integer multiply" in {
      Evaluator.evaluateQuery(Query.parse("i * 2 = 12"), TestRow()) must_== VTrue
    }

    "integer divide" in {
      Evaluator.evaluateQuery(Query.parse("i / 2 = 3"), TestRow()) must_== VTrue
    }

  }

  "Evaluator and" should {
    "match true and true" in {
      Evaluator.evaluateQuery(Query.parse("true and true"), TestRow()) must_== VTrue
    }

    "not match true and false" in {
      Evaluator.evaluateQuery(Query.parse("true and false"), TestRow()) must_== VFalse
    }

    "not match false and true" in {
      Evaluator.evaluateQuery(Query.parse("false and true"), TestRow()) must_== VFalse
    }

    "not match false and false" in {
      Evaluator.evaluateQuery(Query.parse("false and false"), TestRow()) must_== VFalse
    }
  }

  "Evaluator or" should {
    "match true or true" in {
      Evaluator.evaluateQuery(Query.parse("true or true"), TestRow()) must_== VTrue
    }

    "match true or false" in {
      Evaluator.evaluateQuery(Query.parse("true or false"), TestRow()) must_== VTrue
    }

    "match false or true" in {
      Evaluator.evaluateQuery(Query.parse("false or true"), TestRow()) must_== VTrue
    }

    "not match false or false" in {
      Evaluator.evaluateQuery(Query.parse("false or false"), TestRow()) must_== VFalse
    }
  }

  "Evaluator in" should {

    "match in string" in {
      Evaluator.evaluateQuery(Query.parse("s in ('Test')"), TestRow()) must_== VTrue
    }

    "not match not in string" in {
      Evaluator.evaluateQuery(Query.parse("s in ('Test2')"), TestRow()) must_== VFalse
    }

    "match in integer" in {
      Evaluator.evaluateQuery(Query.parse("i in (6)"), TestRow()) must_== VTrue
    }

    "not match not in integer" in {
      Evaluator.evaluateQuery(Query.parse("i in (5)"), TestRow()) must_== VFalse
    }

    "match in boolean" in {
      Evaluator.evaluateQuery(Query.parse("b in (true)"), TestRow()) must_== VTrue
    }

    "not match not in boolean" in {
      Evaluator.evaluateQuery(Query.parse("b in (false)"), TestRow()) must_== VFalse
    }

    "match in InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid in ('abc123')"), TestRow()) must_== VTrue
    }

    "not match not in InternalId" in {
      Evaluator.evaluateQuery(Query.parse("iid in ('abc1234')"), TestRow()) must_== VFalse
    }

    "match in StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action in ('insert')"), TestRow()) must_== VTrue
    }

    "not match not in StandingQueryAction" in {
      Evaluator.evaluateQuery(Query.parse("action in ('update')"), TestRow()) must_== VFalse
    }

    "match with multiple in parameters" in {
      Evaluator.evaluateQuery(Query.parse("s in ('Test1', 'Test2', 'Test', 'Test3')"), TestRow()) must_== VTrue
    }

  }

  section("unit")

  case class TestRow(
    s: String = "Test",
    i: Int = 6,
    b: Boolean = true,
    d: DateTime = new DateTime(2014, Month.June, 3, 11, 4),
    iid: InternalId = InternalId("abc123"),
    action: StandingQueryAction = StandingQueryAction.Insert
  ) extends com.qoid.bennu.ToJsonCapable
}
