package org.openurp.edu.eams.teach.other.service

import java.util.Collection
import java.util.List
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.code.industry.OtherExamCategory
import org.openurp.edu.eams.teach.code.industry.OtherExamSubject
import org.openurp.edu.eams.teach.other.OtherGrade

import scala.collection.JavaConversions._

trait OtherGradeService {

  def saveOrUpdate(otherGrade: OtherGrade): Unit

  def getBestGrade(std: Student, category: OtherExamCategory): OtherGrade

  def getPassGradesOf(std: Student, otherExamSubjects: Collection[OtherExamSubject]): List[OtherGrade]

  def isPass(std: Student, subject: OtherExamSubject): Boolean

  def getOtherGrades(std: Student, isBest: java.lang.Boolean): Collection[OtherGrade]
}
