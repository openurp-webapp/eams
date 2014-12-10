package org.openurp.eams.action.code

import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.code.{TeacherState, TeacherType, TeacherUnitType, TutorType}
import org.openurp.teach.code.StdStatus

class StdStatusAction extends RestfulAction[StdStatus] 
class TeacherStateAction extends RestfulAction[TeacherState] 
class TeacherTypeAction extends RestfulAction[TeacherType] 
class TeacherUnitTypeAction extends RestfulAction[TeacherUnitType] 
class TutorTypeAction extends RestfulAction[TutorType] 