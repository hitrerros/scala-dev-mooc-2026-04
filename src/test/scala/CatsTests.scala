import cats.syntax.functor.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import ru.otus.module2.catsHomework.*

import scala.util.Failure

class CatsTests extends AnyFunSuite with Matchers{

  test("tree functor") {
    val srcTree: Tree[Int] = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    val destTree: Tree[Int] = Branch(Leaf(2), Branch(Leaf(4), Leaf(6)))
    assert(srcTree.map(_ * 2) == destTree)

  }

  test("monad error ") {
    val srcExc = Failure(new Exception("thrown"))
    val destExc = Failure(new Exception("restored"))

    import ru.otus.module2.catsHomework.tryMonad
    assert(tryMonad.handleErrorWith(srcExc)(_ => destExc) == destExc)
  }

  test("either monad") {
     val left = Left("error")
     val right = Right("restored")

     assert(eitherMonad.handleError(left)(_ => "restored") == right)
  }


}
