package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class ElectableLessonByTeachClassFilter extends AbstractElectableLessonFilter() {

  order = AbstractElectRuleExecutor.Priority.FIFTH.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (retakeService.isRetakeCourse(state, lesson.getCourse.getId) && 
      !retakeService.isCheckTeachClass(state.getProfile(entityDao).getElectConfigs)) {
      return true
    }
    if (CollectUtils.isEmpty(lesson.getTeachClass.getLimitGroups)) {
      return true
    }
    CourseLimitGroupHelper.isElectable(lesson, state)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("只开放给:" + context.getLesson.getTeachClass.getName + "的学生", 
        ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }
}
