package org.openurp.edu.eams.teach.lesson.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Iterator
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.eams.teach.lesson.ExamActivity
import org.openurp.edu.eams.teach.lesson.ExamRoom
import ExamActivityDigestor._

import scala.collection.JavaConversions._

object ExamActivityDigestor {

  val singleTeacher = ":teacher1"

  val multiTeacher = ":teacher+"

  val moreThan1Teacher = ":teacher2"

  val day = ":day"

  val date = ":date"

  val units = ":units"

  val weeks = ":weeks"

  val time = ":time"

  val room = ":room"

  val building = ":building"

  val district = ":district"

  val defaultFormat = ":date :time 第:weeks周 :day"

  val df = new SimpleDateFormat("yyyy-MM-dd")

  def getInstance(): ExamActivityDigestor = new ExamActivityDigestor()
}

class ExamActivityDigestor private () {

  private var delimeter: String = ","

  def digest(activity: ExamActivity, resource: TextResource): String = {
    digest(activity, resource, defaultFormat)
  }

  def digest(activity: ExamActivity, resource: TextResource, format: String): String = {
    if (null == activity) return ""
    if (Strings.isEmpty(format)) {
      format = defaultFormat
    }
    val hasRoom = Strings.contains(format, room)
    val hasTeacher = Strings.contains(format, "teacher")
    val arrangeInfoBuf = new StringBuffer()
    var iter = activity.getExamRooms.iterator()
    while (iter.hasNext) {
      val examRoom = iter.next()
      arrangeInfoBuf.append(format)
      var replaceStart = 0
      replaceStart = arrangeInfoBuf.indexOf(day)
      if (-1 != replaceStart) {
        var dayIndex = activity.getStartAt.getDay
        if (dayIndex == 0) {
          dayIndex = 6
        } else {
          dayIndex -= 1
        }
        arrangeInfoBuf.replace(replaceStart, replaceStart + day.length, WeekDays.All(dayIndex).getName)
      }
      replaceStart = arrangeInfoBuf.indexOf(units)
      if (-1 != replaceStart) {
      }
      replaceStart = arrangeInfoBuf.indexOf(time)
      if (-1 != replaceStart) {
        val sdm = new SimpleDateFormat("HH:mm")
        arrangeInfoBuf.replace(replaceStart, replaceStart + time.length, sdm.format(activity.getStartAt) + "-" + sdm.format(activity.getEndAt))
      }
      replaceStart = arrangeInfoBuf.indexOf(date)
      if (-1 != replaceStart) {
        arrangeInfoBuf.replace(replaceStart, replaceStart + date.length, df.format(activity.getStartAt))
      }
      replaceStart = arrangeInfoBuf.indexOf(weeks)
      if (-1 != replaceStart) {
        val teachWeek = EamsDateUtil.SUNDAY_FIRST.getWeekOfYear(examRoom.getsemester.beginOn)
        val examWeek = EamsDateUtil.SUNDAY_FIRST.getWeekOfYear(activity.getStartAt)
        val c = Calendar.getInstance
        c.setTime(activity.getStartAt)
        if (c.get(Calendar.YEAR) > activity.getSemester.getStartYear) {
          val year = SemesterUtil.getStartYear(activity.getSemester)
          val LastDay = year + "-12-31"
          val gregorianCalendar = new GregorianCalendar()
          gregorianCalendar.setTime(java.sql.Date.valueOf(LastDay))
          var endAtSat = false
          if (gregorianCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            endAtSat = true
          }
          var yearWeeks = 53
          if (!endAtSat) {
            yearWeeks += 1
          }
          arrangeInfoBuf.replace(replaceStart, replaceStart + weeks.length, yearWeeks - teachWeek + examWeek + "")
        } else {
          arrangeInfoBuf.replace(replaceStart, replaceStart + weeks.length, examWeek - teachWeek + 1 + "")
        }
      }
      replaceStart = arrangeInfoBuf.indexOf(room)
      if (-1 != replaceStart) {
        arrangeInfoBuf.replace(replaceStart, replaceStart + room.length, if ((null != examRoom.getRoom)) examRoom.getRoom.getName else "")
        replaceStart = arrangeInfoBuf.indexOf(building)
        if (-1 != replaceStart) {
          if (null != examRoom.getRoom && null != examRoom.getRoom.getBuilding) {
            arrangeInfoBuf.replace(replaceStart, replaceStart + building.length, examRoom.getRoom.getBuilding.getName)
          } else {
            arrangeInfoBuf.replace(replaceStart, replaceStart + building.length, "")
          }
        }
        replaceStart = arrangeInfoBuf.indexOf(district)
        if (-1 != replaceStart) {
          if (null != examRoom.getRoom && null != examRoom.getRoom.getBuilding && 
            null != examRoom.getRoom.getBuilding.getCampus) {
            arrangeInfoBuf.replace(replaceStart, replaceStart + district.length, examRoom.getRoom.getBuilding.getCampus.getName)
          } else {
            arrangeInfoBuf.replace(replaceStart, replaceStart + district.length, "")
          }
        }
      }
      arrangeInfoBuf.append(delimeter)
    }
    if (arrangeInfoBuf.lastIndexOf(delimeter) != -1) arrangeInfoBuf.delete(arrangeInfoBuf.lastIndexOf(delimeter), 
      arrangeInfoBuf.length)
    arrangeInfoBuf.toString
  }

  def setDelimeter(delimeter: String): ExamActivityDigestor = {
    this.delimeter = delimeter
    this
  }
}
