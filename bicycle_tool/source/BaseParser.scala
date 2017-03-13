package bycicle_tool


import scala.util.parsing.combinator._

class BaseParser extends RegexParsers {

  def surround[T](left: Parser[Any], right: Parser[Any], inner: Parser[T]): Parser[T] = left ~> inner <~ right

  // Like 'repsep' and 'rep1sep' except allows an optional trailing separator.
  def repsepend[T,U](inner: Parser[T], sep: Parser[U]): Parser[Seq[T]] = rep1sepend(inner, sep) | success(Seq.empty)
  def rep1sepend[T,U](inner: Parser[T], sep: Parser[U]): Parser[Seq[T]] = rep1sep(inner, sep) <~ opt(sep)

  def commented[T](inner: Parser[T]): Parser[T] = surround("/*", "*/", inner)
  def brased[T](inner: Parser[T]): Parser[T] = surround("{", "}", inner)
  def parens[T](inner: Parser[T]): Parser[T] = surround("(", ")", inner)

  def ident: Parser[String] = """[A-Za-z_0-9]*""".r

  def fileName: Parser[String] = """[A-Za-z_0-9_]*\.[A-Za-z_0-9_]*""".r
  def path: Parser[String] = """[A-Za-z_0-9_./]*""".r
  def numeric: Parser[String] = """[0-9]+""".r

  def maybeQuoted(inner: Parser[String]): Parser[String] = opt("\"") ~ inner ~ opt("\"") ^^ {
    case q1 ~ inner ~ q2 => {
      var result = ""
      if (q1.isDefined)
        result += "\""
      result += inner
      if (q2.isDefined)
        result += "\""
      result
    }
  }

}
