package org.openurp.edu.eams.teach.planaudit.service


import org.beangle.ems.rule.model.SimpleContext
import org.openurp.edu.base.Student
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.teach.plan.CoursePlan


class PlanAuditContext extends SimpleContext {

  
  var std: Student = _

  
  var standard: PlanAuditStandard = _

  
  var coursePlan: CoursePlan = _

  
  var result: PlanAuditResult = _

  
  var stdGrade: StdGrade = _

  
  var instantAudit: Boolean = _

  
  var auto: Boolean = _

  
  var partial: Boolean = _

  
  var auditTerms: Array[String] = _

  def getParam[T](paramName: String, clazz: Class[T]): T = {
    getParams.get(paramName).asInstanceOf[T]
  }

  def this(params: Map[String, Any]) {
    super()
    getParams.putAll(params)
  }

  def this(coursePlan: CoursePlan, stdGrade: StdGrade, result: PlanAuditResult) {
    super()
    this.coursePlan = coursePlan
    this.stdGrade = stdGrade
    this.result = result
  }

  def setAuditTerms(auditTerms: Array[String]) {
    this.auditTerms = auditTerms
    this.partial = if (auditTerms == null || auditTerms.length == 0) false else true
  }
}
