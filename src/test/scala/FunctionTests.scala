import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import ru.otus.module1.opt._
import ru.otus.module1.list._


class FunctionTests  extends AnyFunSuite with Matchers {

  test("Option zip test") {
    Some(1).zip(Some(2)) shouldBe Some((1, 2))
    Some(1).zip(None) shouldBe None
    None.zip(Some(1)) shouldBe None
  }

  test("Option filter test") {
    Some(4).filter(_ > 3) shouldBe Some(4)
    Some(4).filter(_ > 5) shouldBe None
    None.filter(_ == 2) shouldBe None
  }

  test(":: prepend tested") {
    1 :: List(2, 4, 6) shouldBe List(1, 2, 4, 6)
  }

  test("list reverse test") {
    reverse(List(2, 4, 6)) shouldBe List(6, 4, 2)
  }

  test("list map") {
    List(1, 3, 4).map(k => k * 2) shouldBe List(2, 6, 8)
  }

  test("list filter") {
    List(-1, 0, 3, 4).filter(_ > 0) shouldBe List(3, 4)
  }

  test("list incList") {
    incList(List(1, 2, 4)) shouldBe List(2, 3, 5)

  }
  test("shoutString list ") {
    shoutString(List("aa", "bb", "cc")) shouldBe List("!aa", "!bb", "!cc")
  }

  test("mkString list ") {
    List("aa", "bb", "cc").mkString(",") shouldBe "aa,bb,cc"
  }

  test("flatmap test ") {
    List(1, 2, 3).flatmap(k => List(k, k)) shouldBe List(1, 1, 2, 2, 3, 3)
  }


}
