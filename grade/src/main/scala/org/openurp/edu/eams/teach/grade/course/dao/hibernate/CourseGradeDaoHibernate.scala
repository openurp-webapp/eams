package org.openurp.edu.eams.teach.grade.course.dao.hibernate

import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.course.dao.CourseGradeDao
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class CourseGradeDaoHibernate extends HibernateEntityDao with CourseGradeDao {

  protected var courseGradeCalculator: CourseGradeCalculator = _

  def needReStudy(std: Student, course: Course): Boolean = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std.id = :stdId", std.getId)
    query.where("grade.course.id = :courseId", course.getId)
    query.select("grade.passed")
    val rs = search(query).asInstanceOf[Collection[Boolean]]
    if (CollectUtils.isEmpty(rs)) {
      false
    } else {
      rs.find(true == _).map(_ => false).getOrElse(true)
    }
  }

  def getGradeCourseMap(stdId: java.lang.Long): Map[Any, Boolean] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "cg")
    query.where("cg.std.id = :stdId", stdId)
    query.select("cg.course.id,cg.passed")
    val rs = search(query).asInstanceOf[Collection[Array[Any]]]
    val courseMap = CollectUtils.newHashMap()
    for (obj <- rs) {
      if (null != obj(1)) {
        courseMap.put(obj(0), obj(1).asInstanceOf[java.lang.Boolean])
      } else {
        courseMap.put(obj(0), false)
      }
    }
    courseMap
  }

  def removeExamGrades(lesson: Lesson, gradeType: GradeType) {
  }

  def removeExamGrades(lesson: Lesson, gradeType: GradeType, isClearState: Boolean) {
    if (gradeType.getId.longValue() == GradeTypeConstants.GA_ID || 
      gradeType.getId.longValue() == GradeTypeConstants.FINAL_ID) {
      removeGrades(lesson, isClearState)
      return
    }
    val stateQuery = OqlBuilder.from(classOf[CourseGradeState], "courseGradeState")
    stateQuery.where("courseGradeState.lesson = :lesson", lesson)
    val courseGradeStates = search(stateQuery.build())
    if (!courseGradeStates.isEmpty) {
      val state = courseGradeStates.get(0)
      val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
      query.where("courseGrade.lesson = :lesson", lesson)
      query.join("courseGrade.examGrades", "examGrade")
      query.where("examGrade.gradeType = :gradeType", gradeType)
      val courseGrades = search(query)
      for (courseGrade <- courseGrades) {
        courseGrade.getExamGrades.remove(courseGrade.getExamGrade(gradeType))
        courseGradeCalculator.calc(courseGrade, state)
      }
      val toBeSave = CollectUtils.newArrayList()
      toBeSave.addAll(courseGrades)
      if (isClearState) {
        state.getStates.remove(state.getState(gradeType))
      } else {
        state.updateStatus(gradeType, Grade.Status.NEW)
      }
      toBeSave.add(state)
      saveOrUpdate(toBeSave)
    }
  }

  def removeGrades(lesson: Lesson) {
    removeGrades(lesson, true)
  }

  def removeGrades(lesson: Lesson, isClearAll: Boolean) {
    val stateQuery = OqlBuilder.from(classOf[CourseGradeState], "courseGradeState")
    stateQuery.where("courseGradeState.lesson = :lesson", lesson)
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson", lesson)
    val courseGradeStates = search(stateQuery.build())
    for (courseGradeState <- courseGradeStates) {
      courseGradeState.setStatus(Grade.Status.NEW)
      if (isClearAll) {
        courseGradeState.getStates.clear()
      } else {
        for (examGradeState <- courseGradeState.getStates) {
          examGradeState.setStatus(Grade.Status.NEW)
        }
      }
      saveOrUpdate(courseGradeState)
    }
    remove(search(query))
  }

  def publishCourseGrade(lesson: Lesson, isPublished: java.lang.Boolean) {
    val hql = new StringBuilder()
    hql.append("update CourseGrade set status=")
    hql.append((if (true == isPublished) Grade.Status.PUBLISHED else Grade.Status.CONFIRMED))
    hql.append(" where lesson.id=" + lesson.getId)
    getSession.createQuery(hql.toString).executeUpdate()
  }

  def publishExamGrade(lesson: Lesson, gradeType: GradeType, isPublished: java.lang.Boolean) {
    val hql = new StringBuilder()
    hql.append("update ExamGrade set status=")
    hql.append(if (true == isPublished) Grade.Status.PUBLISHED else Grade.Status.CONFIRMED)
    hql.append("where gradeType.id=" + gradeType.getId)
    hql.append(" exists(from CourseGrade cg where cg.lesson.id=" + lesson.getId)
    hql.append("and cg.id=courseGrade.id)")
    getSession.createQuery(hql.toString).executeUpdate()
  }

  def publishExamGrade(lesson: Lesson, gradeTypes: List[GradeType], isPublished: java.lang.Boolean) {
    val gradeTypeIds = new StringBuffer(",")
    for (gradeType <- gradeTypes) {
      gradeTypeIds.append(gradeType.getId).append(",")
    }
    val hql = new StringBuilder()
    hql.append("update ExamGrade set status=")
    hql.append(if (true == isPublished) Grade.Status.PUBLISHED else Grade.Status.CONFIRMED)
    hql.append(" where id in (select id from ExamGrade where instr('" + 
      gradeTypeIds)
    hql.append("',','||gradeType.id||',')>0 " + " and courseGrade.lesson.id=" + 
      lesson.getId + 
      ")")
    getSession.createQuery(hql.toString).executeUpdate()
  }

  def setCourseGradeCalculator(courseGradeCalculator: CourseGradeCalculator) {
    this.courseGradeCalculator = courseGradeCalculator
  }
}
