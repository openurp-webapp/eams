package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.weekstate.WeekStateDirection.LTR
import org.openurp.edu.eams.weekstate.SemesterWeekState.RESERVE_BITS
import org.openurp.edu.eams.weekstate.WeekStateDirection.RTL
import java.util.Collection
import java.util.Date
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.eams.date.EamsWeekday
import org.openurp.edu.eams.date.RelativeDateUtil
import org.openurp.edu.eams.exception.WeekStateException
import SemesterWeekStateBuilder._

import scala.collection.JavaConversions._

object SemesterWeekStateBuilder {

  def LTR(semester: Semester): SemesterWeekStateBuilder = {
    val helper = new SemesterWeekStateBuilder()
    helper.direction = WeekStateDirection.LTR
    helper.paddingLength = BasicWeekState.MAX_LENGTH
    helper.semester = semester
    if (semester != null) {
      helper.rdateUtil = RelativeDateUtil.startOn(semester)
    }
    helper
  }

  def RTL(semester: Semester): SemesterWeekStateBuilder = {
    val helper = new SemesterWeekStateBuilder()
    helper.direction = WeekStateDirection.RTL
    helper.paddingLength = 0
    helper.semester = semester
    if (semester != null) {
      helper.rdateUtil = RelativeDateUtil.startOn(semester)
    }
    helper
  }

  def merge(state1: SemesterWeekState, state2: SemesterWeekState): SemesterWeekState = {
    if (state1 == null && state2 == null) {
      return null
    }
    if (state1 == null) {
      return state2.clone()
    } else if (state2 == null) {
      return state1.clone()
    }
    if (state1.direction != state2.direction) {
      throw new WeekStateException("Merge Error: Direction Different")
    } else if (state1.getWeekday != state2.getWeekday) {
      throw new WeekStateException("Merge Error: Weekday Different")
    } else if (state1.getSemester != state2.getSemester) {
      throw new WeekStateException("Merge Error: Semester Different")
    } else if (state1.getReserveBits != state2.getReserveBits) {
      throw new WeekStateException("Merge Error: Reserve Bits Different")
    }
    val res = new SemesterWeekState(state1)
    res.setWeekState(state1.getNumber | state2.getNumber)
    res
  }

  def merge(states: Array[SemesterWeekState]): SemesterWeekState = {
    if (states == null || states.length == 0) {
      return null
    }
    if (states.length == 1) {
      return states(0)
    }
    var res = states(0)
    for (i <- 1 until states.length) {
      res = merge(res, states(i))
    }
    res
  }

  def merge(states: List[SemesterWeekState]): SemesterWeekState = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray(Array.ofDim[SemesterWeekState](0)))
  }

  def merge(states: Collection[SemesterWeekState]): SemesterWeekState = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray(Array.ofDim[SemesterWeekState](0)))
  }

  def parse(weekState: String, direction: WeekStateDirection): Array[Integer] = {
    if (direction == RTL) {
      weekState = new StringBuilder(weekState).reverse().toString
    }
    val weekIndecies = CollectUtils.newArrayList()
    var i = 0
    while (i != -1) {
      i = weekState.indexOf('1', i)
      if (i == -1) {
        //break
      }
      weekIndecies.add(i)
      i = i + 1
    }
    for (j <- 0 until weekIndecies.size) {
      var weekIndex = weekIndecies.get(j)
      weekIndex = if (weekIndex >= RESERVE_BITS) weekIndex - RESERVE_BITS + 1 else weekIndex - RESERVE_BITS
      weekIndecies.set(j, weekIndex)
    }
    weekIndecies.toArray(Array.ofDim[Integer](0))
  }

  def parse(weekState: java.lang.Long, direction: WeekStateDirection): Array[Integer] = {
    parse(BinaryConverter.toString(weekState), direction)
  }
}

class SemesterWeekStateBuilder private () extends RelativeWeekStateBuilder() {

  private var direction: WeekStateDirection = _

  private var paddingLength: java.lang.Integer = _

  private var semester: Semester = _

  private var rdateUtil: RelativeDateUtil = _

  def build(date: Date): SemesterWeekState = {
    build(Array(rdateUtil.getWeekIndex(date)), EamsDateUtil.getWeekday(date))
  }

  def build(relativeWeekIndecies: Array[Int], weekday: EamsWeekday): SemesterWeekState = {
    if (relativeWeekIndecies == null || relativeWeekIndecies.length == 0) {
      return null
    }
    val weekState = new SemesterWeekState()
    weekState.setSemester(semester)
    weekState.setDirection(direction)
    weekState.setReserveBits(RESERVE_BITS)
    weekState.setWeekState(buildString(relativeWeekIndecies))
    weekState.setWeekday(weekday)
    weekState
  }

  def build(relativeWeekIndecies: Array[Integer], weekday: EamsWeekday): SemesterWeekState = {
    if (relativeWeekIndecies == null || relativeWeekIndecies.length == 0) {
      return null
    }
    val weekIndeciesArray = Array.ofDim[Int](relativeWeekIndecies.length)
    for (i <- 0 until relativeWeekIndecies.length) {
      weekIndeciesArray(i) = relativeWeekIndecies(i)
    }
    build(weekIndeciesArray, weekday)
  }

  def build(relativeWeekIndecies: String, weekday: EamsWeekday): SemesterWeekState = {
    build(Strings.splitToInt(relativeWeekIndecies), weekday)
  }

  private def convertFrom(yearWeekState: YearWeekState): SemesterWeekState = {
    if (yearWeekState == null) {
      return null
    }
    var year_weekStateString = yearWeekState.getString
    if (yearWeekState.direction == RTL) {
      year_weekStateString = new StringBuilder(year_weekStateString).reverse().toString
    }
    val weekday = yearWeekState.getWeekday
    val sem_weekIndecies = CollectUtils.newArrayList()
    var oneIndex = -1
    while (oneIndex < year_weekStateString.length) {
      oneIndex = year_weekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        //break
      }
      val year_weekIndex = oneIndex + 1
      val date = EamsDateUtil.SUNDAY_FIRST.getDate(yearWeekState.getYear, year_weekIndex, weekday)
      val sem_weekIndex = rdateUtil.getWeekIndex(date)
      if (sem_weekIndex < -RESERVE_BITS) {
        throw new WeekStateException("Convert Error: weekIndex is less than -" + RESERVE_BITS + 
          " weeks")
      }
      if (sem_weekIndex > BasicWeekState.MAX_LENGTH) {
        throw new WeekStateException("Convert Error: weekIndex is greater than " + BasicWeekState.MAX_LENGTH)
      }
      sem_weekIndecies.add(sem_weekIndex)
    }
    build(sem_weekIndecies.toArray(Array.ofDim[Integer](0)), yearWeekState.getWeekday)
  }

  def convertFrom(yearWeekStates: Array[YearWeekState]): SemesterWeekState = {
    if (yearWeekStates == null || yearWeekStates.length == 0) {
      return null
    }
    val weekday = yearWeekStates(0).getWeekday
    for (i <- 1 until yearWeekStates.length if weekday != yearWeekStates(i).getWeekday) {
      throw new WeekStateException("Convert error: weekday should be same")
    }
    val states = CollectUtils.newArrayList()
    for (i <- 0 until yearWeekStates.length) {
      val state = convertFrom(yearWeekStates(i))
      if (state != null) {
        states.add(state)
      }
    }
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    val state = states.get(0)
    var number = state.getNumber
    for (i <- 1 until states.size) {
      number = number | states.get(i).getNumber
    }
    state.setWeekState(number)
    state
  }

  def convertFrom(yearWeekStates: List[YearWeekState]): SemesterWeekState = {
    convertFrom(yearWeekStates.toArray(Array()))
  }

  def convertFrom(yearWeekStates: Collection[YearWeekState]): SemesterWeekState = {
    convertFrom(yearWeekStates.toArray(Array()))
  }

  protected def buildString(weekIndecies: Array[Int]): String = {
    val res = Strings.repeat("0", BasicWeekState.MAX_LENGTH)
    val originCharAt = RESERVE_BITS
    val sb = new StringBuilder(res)
    for (i <- 0 until weekIndecies.length) {
      val weekIndex = weekIndecies(i)
      var charAt = 0
      charAt = if (weekIndex <= 0) originCharAt + weekIndex else originCharAt + weekIndex - 1
      if (charAt < 0) {
        throw new WeekStateException("WeekIndex is less then -" + RESERVE_BITS + " weeks")
      }
      if (charAt < RESERVE_BITS) {
        sb.setCharAt(charAt, '1')
      } else {
        if (charAt + 1 > sb.length) {
          sb.append(Strings.repeat('0', charAt + 1 - sb.length))
        }
        sb.setCharAt(charAt, '1')
      }
    }
    if (this.direction == LTR) {
      Strings.rightPad(sb.toString, this.paddingLength, '0')
    } else {
      Strings.leftPad(sb.reverse().toString.replaceAll("^0+1", "1"), this.paddingLength, '0')
    }
  }

  def parse(weekState: String): Array[Integer] = parse(weekState, this.direction)

  def parse(weekState: java.lang.Long): Array[Integer] = parse(weekState, this.direction)
}
