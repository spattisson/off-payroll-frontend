/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.offpayroll.util

import uk.gov.hmrc.offpayroll.models._

import scala.annotation.tailrec
import scala.collection.immutable.BitSet

object InterviewCompressor extends App {

  def encodeYesNo(value:String) = if (value.toLowerCase == "yes") 2 else 1

  private def encodeElementValue(value: String, element: Element):Int = {
    element.children match {
      case Nil => encodeYesNo(value)
      case children => children.find(_.questionTag == value).map(_.order + 1).getOrElse(0)
    }
  }

  private def encodeElementValues(values: List[String], element: Element): (Int, Int) = {
    val booleans = for {
      child <- element.children
    } yield {
      values.find(_ == child.questionTag).map(_ => true).getOrElse(false)
    }
    val indices = booleans.zipWithIndex.collect{ case (a, i) if a => i}
    (BitSet(indices:_*).toBitMask.head.toInt, booleans.size)
  }

  def elementBitWidth(element: Element): Int = {
    if (element.children.isEmpty) 2
    else MsbEvaluator.msbPos(element.children.size + 1)
  }

  def encodeValues(values: List[String], element: Element): (Int,Int) = {
    element.elementType match {
      case GROUP => encodeElementValues(values, element)
      case _ => (encodeElementValue(values(0), element), elementBitWidth(element))
    }
  }

  def encodeMultiValues(multivalues: List[List[String]]): List[(Int,Int)] = {
    val elements = OffPayrollWebflow.clusters.flatMap(_.clusterElements)
    val pairs = multivalues.zip(elements)
    pairs.map { case (values, element) => encodeValues(values, element) }
  }


}


object MsbEvaluator extends App {
  def msbPos(n: Int): Int = {
    @tailrec
    def go(n: Int, acc: Int): Int = {
      if (n == 0) acc else go(n >> 1, acc + 1)
    }
    go(n, 0)
  }

  val x = msbPos(1)
  println(x)

}


object ValuesPairsToLongEvaluator extends App {
  def valuesPairsToLong2(p: List[(Int, Int)]): Long = {
    var l = 0L
    for ( (v, w) <- p){
      l = l | v
      println(s"${l.toBinaryString} $w}")
      l = l << w
    }
    l
  }
  def valuesWidthPairsToLong(p: List[(Int, Int)]): Long = {
    @tailrec
    def go(p: List[(Int, Int)], acc: Long): Long = p match {
      case Nil => acc
      case (v,w)::xs => go(xs, (acc << w) | v)
    }
    go(p, 0L)
  }

  def longAndWidthsFlatToValueWidthPairs(l: Long, widths: List[Int]): List[(Int, Int)] = {
    def go(l:Long, widths: List[Int], acc: List[(Int, Int)]): List[(Int, Int)] = widths match {
      case Nil => List()
      case w::Nil => (l.toInt,w) :: acc
      case w::xs => go(l >> w, xs, ((l & ((1 << w) - 1)).toInt,w) :: acc)
    }
    go(l, widths.reverse, List())
  }

  val pp = List((3,3), (1,2), (2,2), (2,2), (2,2), (2,3), (3,3), (3,3), (4,3), (2,2), (0,2), (0,2), (0,2), (2,2), (4,3), (4,3), (2,2), (2,2), (2,2), (3,3))

  //11001101010100110111000100000001100100010101011
  println(valuesWidthPairsToLong(pp).toBinaryString)
//  println(valuesWidthPairsToLong(pp))
  println(pp)
  println(longAndWidthsFlatToValueWidthPairs(120163403909459L, List(3, 2, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2, 2, 2, 3, 3, 2, 2, 2, 3)))

  val base = 62
  val baseString: String = ((0 to 9) ++ ('A' to 'Z') ++ ('a' to 'z')).mkString

  def encode(i: Long): String = {

    @scala.annotation.tailrec
    def div(i: Long, res: List[Int] = Nil): List[Int] = {
      (i / base) match {
        case q if q > 0 => div(q, (i % base).toInt :: res)
        case _ => i.toInt :: res
      }
    }

    div(i).map(x => baseString(x)).mkString
  }

  def decode(s: String): Long = {
    s.zip(s.indices.reverse)
      .map { case (c, p) => baseString.indexOf(c) * scala.math.pow(base, p).toLong }
      .sum
  }

  def widthsFlat: List[Int] = {
    val GROUP_ITEM_VALUE_BIT_WIDTH = 2
    val elements = OffPayrollWebflow.clusters.flatMap(_.clusterElements)
    elements.flatMap { element =>
      element.elementType match {
        case GROUP => element.children.map(_ => GROUP_ITEM_VALUE_BIT_WIDTH)
        case _ => List(InterviewCompressor.elementBitWidth(element))
      }
    }
  }

  def widths: List[List[Int]] = {
    val GROUP_ITEM_VALUE_BIT_WIDTH = 2
    val elements = OffPayrollWebflow.clusters.flatMap(_.clusterElements)
    elements.map { element =>
      element.elementType match {
        case GROUP => element.children.map(_ => GROUP_ITEM_VALUE_BIT_WIDTH)
        case _ => List(InterviewCompressor.elementBitWidth(element))
      }
    }
  }

  def elementIndex(element: Element): Option[Int] = {
    val elements = OffPayrollWebflow.clusters.flatMap(_.clusterElements)
    val found = elements.zipWithIndex.collect{ case (e, i) if (e == element) => i }
    found.headOption
  }


  println(encode(120163403909459L))
  println(decode("Y7XjYxCV"))
  println(longAndWidthsFlatToValueWidthPairs(decode("Y7XjYxCV"), List(3, 2, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2, 2, 2, 3, 3, 2, 2, 2, 3)))
  println(widthsFlat)
  println(widths)
  println(widths(elementIndex(FinancialRiskCluster.clusterElements(0)).getOrElse(0)))
  println(elementIndex(PartAndParcelCluster.clusterElements(3)).getOrElse(0))

}
