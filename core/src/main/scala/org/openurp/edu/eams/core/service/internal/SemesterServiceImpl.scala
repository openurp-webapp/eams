package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import java.util.ArrayList
import java.util.Collection
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.List
import java.util.Map
import javax.persistence.EntityNotFoundException
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Calendar
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.model.SemesterBean
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.SemesterService

import scala.collection.JavaConversions._

class SemesterServiceImpl extends BaseServiceImpl with SemesterService {

  def getSemester(id: java.lang.Integer): Semester = {
    val semester = entityDao.get(classOf[Semester], id).asInstanceOf[Semester]
    semester
  }

  def getCalendar(project: Project): Calendar = project.getCalendar

  def getCalendars(projects: List[Project]): List[Calendar] = {
    val query = OqlBuilder.from(classOf[Project], "project").where("project in (:projects))", projects)
    query.select("distinct project.calendar")
    entityDao.search(query)
  }

  def getSemester(project: Project, schoolYear: String, name: String): Semester = {
    val calendar = getCalendar(project)
    getSemester(calendar, schoolYear, name)
  }

  def getSemestersOfOverlapped(semester: Semester): List[Semester] = {
    val builder = OqlBuilder.from(classOf[Semester], "semester")
    builder.where("semester.beginOn <= :endOn", semester.getEndOn)
    builder.where("semester.endOn >= :beginOn", semester.beginOn)
    builder.cacheable(true)
    entityDao.search(builder)
  }

  def getSemester(calendar: Calendar, date: Date): Semester = {
    val params = CollectUtils.newHashMap()
    params.put("calendar", calendar)
    params.put("date", date)
    val builder = OqlBuilder.from(classOf[Semester], "semester").where("semester.beginOn<=:date and semester.endOn>=:date and semester.calendar=:calendar")
    builder.params(params).cacheable()
    val rs = entityDao.search(builder)
    if (rs.size < 1) {
      null
    } else {
      rs.get(0).asInstanceOf[Semester]
    }
  }

  def getSemester(calendar: Calendar, begOn: Date, endOn: Date): Semester = {
    val params = CollectUtils.newHashMap()
    params.put("calendar", calendar)
    params.put("begOn", begOn)
    params.put("endOn", endOn)
    val builder = OqlBuilder.from(classOf[Semester], "semester").where("semester.beginOn<=:endOn and semester.endOn>=:begOn and semester.calendar=:calendar")
      .orderBy("semester.beginOn")
    builder.params(params)
    val rs = entityDao.search(builder)
    if (rs.size < 1) {
      null
    } else {
      rs.get(0).asInstanceOf[Semester]
    }
  }

  def getSemester(calendar: Calendar, schoolYear: String, name: String): Semester = {
    val query = OqlBuilder.from(classOf[Semester], "semester")
    query.where("semester.calendar=:calendar", calendar)
    query.where("semester.schoolYear=:schoolYear", schoolYear)
    query.where("semester.name=:name", name)
    val semesters = entityDao.search(query)
    if (semesters.isEmpty) {
      null
    } else {
      semesters.get(0).asInstanceOf[Semester]
    }
  }

  def getNextSemester(semester: Semester): Semester = {
    val nextQuery = OqlBuilder.from(classOf[Semester], "s")
    nextQuery.where("s.calendar=:calendar", semester.getCalendar)
    nextQuery.where("s.beginOn>:beginOn", semester.getEndOn)
      .orderBy("s.beginOn")
      .limit(1, 1)
    val nexts = entityDao.search(nextQuery)
    if (nexts.isEmpty) null else nexts.get(0)
  }

  def getPreviousSemester(calendar: Calendar): Semester = {
    val params = new HashMap()
    params.put("calendar", calendar)
    val rs = entityDao.search("@getPreviousSemester", params)
    if (rs.size < 1) throw new EntityNotFoundException("without schoolYear for calendar id:" + calendar)
    rs.get(0).asInstanceOf[Semester]
  }

  def getCurSemester(calendar: Calendar): Semester = {
    val builder = OqlBuilder.from(classOf[Calendar], "calender").where("calender.id = :calenderId", calendar.getId)
    builder.join("calender.semesters", "semester").where("semester.beginOn <= :date and semester.endOn >= :date", 
      new java.util.Date())
    builder.select("semester")
    val rs = entityDao.search(builder)
    if (rs.size == 1) {
      rs.get(0).asInstanceOf[Semester]
    } else {
      calendar.getNearest
    }
  }

  def getCurSemester(calendarId: java.lang.Integer): Semester = {
    val calendar = entityDao.get(classOf[Calendar], calendarId)
    if (null == calendar) {
      return null
    }
    getCurSemester(calendar)
  }

  def getTermsBetween(first: Semester, second: Semester, omitSmallTerm: Boolean): Int = {
    if (first.getCalendar != second.getCalendar) return 0
    val query = OqlBuilder.from(classOf[Semester], "semester")
    query.select("count(semester.id)").where("semester.beginOn >= :firstStart")
      .where("semester.beginOn <= :secondStart")
      .where("semester.calendar = :calendar")
      .where("((:omitSmallTerm = true and (year(semester.endOn) * 12 + month(semester.endOn)) - (year(semester.beginOn) * 12 + month(semester.beginOn)) > 2) or (:omitSmallTerm = false))")
    query.param("calendar", first.getCalendar)
    query.param("omitSmallTerm", new java.lang.Boolean(omitSmallTerm))
    query.cacheable()
    val firDate = first.getBeginOn
    val secDate = second.getBeginOn
    if (first.after(second)) {
      val calendar = new GregorianCalendar()
      query.param("firstStart", secDate)
      query.param("secondStart", firDate)
      -entityDao.search(query).get(0).asInstanceOf[Number]
        .intValue()
    } else {
      query.param("firstStart", firDate)
      query.param("secondStart", secDate)
      entityDao.search(query).get(0).asInstanceOf[Number]
        .intValue()
    }
  }

  def removeSemester(semester: Semester) {
    entityDao.remove(semester)
  }

  def saveSemester(semester: Semester) {
    if (null == semester) return
    if (Strings.isEmpty(semester.getCode)) semester.setCode(semester.getSchoolYear + semester.getName)
    entityDao.saveOrUpdate(semester)
  }

  def checkDateCollision(semester: Semester): Boolean = {
    if (null == semester) return false
    val builder = OqlBuilder.from(classOf[Semester], "semester")
    builder.where("semester.calendar=:calendar", semester.getCalendar)
    if (null != semester.getId) builder.where("id <> " + semester.getId)
    val semesterList = entityDao.search(builder)
    for (one <- semesterList if semester.beginOn.before(one.getEndOn) && one.getBeginOn.before(semester.getEndOn)) return true
    false
  }

  def getCurSemester(project: Project): Semester = {
    val calendar = getCalendar(project)
    getCurSemester(calendar)
  }

  def getNearestSemester(project: Project): Semester = {
    val calendar = getCalendar(project)
    getNearestSemester(calendar)
  }

  def getNearestSemester(calendar: Calendar): Semester = {
    val ss = entityDao.search("select id,schoolYear,name,endOn from org.openurp.edu.eams.core.Semester")
    val query = OqlBuilder.from(classOf[Semester], "semester")
    query.where("semester.calendar = :calendar", calendar)
      .where("((semester.beginOn-current_date())*(semester.endOn-current_date())) <= all(select (c.beginOn-current_date())*(c.endOn-current_date())from org.openurp.edu.eams.core.Semester as c where c.calendar =:calendar)", 
      calendar)
    val semesters = entityDao.search(query)
    if (semesters.isEmpty) {
      null
    } else {
      semesters.get(0).asInstanceOf[Semester]
    }
  }

  def getSemesters(semesterStartId: java.lang.Integer, semesterEndId: java.lang.Integer): List[Semester] = {
    var semesterStart = new SemesterBean()
    var semesterEnd = new SemesterBean()
    if (semesterStartId != null) {
      semesterStart = entityDao.get(classOf[Semester], semesterStartId)
    }
    if (semesterEndId != null) {
      semesterEnd = entityDao.get(classOf[Semester], semesterEndId)
    }
    val builder = OqlBuilder.from(classOf[Semester], "semester")
    if (semesterStartId != null && semesterEndId == null) {
      builder.where("semester.beginOn >= :startTime", semesterStart.getBeginOn)
    }
    if (semesterStartId == null && semesterEndId != null) {
      builder.where("semester.beginOn <= :endTime", semesterEnd.getBeginOn)
    }
    if (semesterStartId != null && semesterEndId != null) {
      builder.where("semester.beginOn >= :startTime", semesterStart.getBeginOn)
      builder.where("semester.beginOn <= :endTime", semesterEnd.getBeginOn)
    }
    var semesterList = new ArrayList[Semester]()
    semesterList = entityDao.search(builder)
    semesterList
  }

  def getPrevSemester(semester: Semester): Semester = {
    val query = OqlBuilder.from(classOf[Semester], "semester").where("semester.calendar = :calendar", 
      semester.getCalendar)
      .where("semester.endOn < (select cur.beginOn from " + classOf[Semester].getName + 
      " cur where cur.id = :curId)", semester.getId)
      .orderBy("semester.endOn desc")
      .cacheable()
    val semesters = entityDao.search(query)
    if (CollectUtils.isNotEmpty(semesters)) {
      return semesters.get(0)
    }
    null
  }
}
