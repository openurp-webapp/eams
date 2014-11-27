package org.openurp.eams.grade.teacher.action

class MakeupGaAction extends AbstractTeacherAction {

//  protected def getGradeTypes(gradeState: CourseGradeState): List[GradeType] = {
//    var gradeTypes = getAttribute("gradeTypes").asInstanceOf[List[GradeType]]
//    if (null == gradeTypes) {
//      gradeTypes = CollectUtils.newArrayList()
//      val gradeTypeIds = Array(GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID)
//      val gis = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
//      for (typeId <- gradeTypeIds) {
//        val gradeType = baseCodeService.getCode(classOf[GradeType], typeId).asInstanceOf[GradeType]
//        if (null != gis && gis.getTypes.contains(gradeType)) gradeTypes.add(gradeType)
//      }
//      put("gradeTypes", gradeTypes)
//    }
//    gradeTypes
//  }
//
//  def input(): String = {
//    val result = checkState()
//    if (null != result) {
//      return result
//    }
//    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
//    val msg = checkLessonPermission(lesson)
//    if (null != msg) {
//      return forwardError(msg)
//    }
//    putGradeMap(lesson, getCourseTakes(lesson))
//    val gradeState = getGradeState
//    buildGradeConfig(lesson, getGradeTypes(gradeState))
//    getState(new GradeType(GradeTypeConstants.MAKEUP_ID))
//      .setScoreMarkStyle(gradeState.getScoreMarkStyle)
//    entityDao.save(getGradeState)
//    val putSomeParams = CollectUtils.newHashSet()
//    putSomeParams.add("isTeacher")
//    putSomeParams.add("RESTUDY")
//    putSomeParams.add("GA")
//    putSomeParams.add("REEXAM")
//    putSomeParams.add("NEW")
//    putSomeParams.add("CONFIRMED")
//    putSomeParams.add("gradeConverterConfig")
//    putSomeParams.add("examStatuses")
//    putSomeParams.add("CONFIRM_MODE")
//    putSomeParams.add("USUAL")
//    putSomeParams.add("TWICE_MODE")
//    putSomeParams.add("VIOLATION")
//    putSomeParams.add("CHEAT")
//    putSomeParams.add("ABSENT")
//    putSomeParams.add("DELAY")
//    putSomeParams.add("gradeRateConfigs")
//    put("setting", settings.getSetting(getProject))
//    buildSomeParams(lesson, putSomeParams)
//    put("NormalTakeType", baseCodeService.getCode(classOf[CourseTakeType], CourseTakeType.NORMAL))
//    put("NormalExamStatus", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
//    put("lesson", lesson)
//    forward()
//  }
//
//  protected def getCourseTakes(lesson: Lesson): List[CourseTake] = {
//    makeupStdStrategy.getCourseTakes(lesson)
//  }
//
//  /**
//   * 查看单个教学任务所有补考成绩信息
//   *
//   * @return
//   */
//  def info(): String = {
//    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
//    val msg = checkLessonPermission(lesson)
//    if (null != msg) {
//      return forwardError(msg)
//    }
//    val takes = getCourseTakes(lesson)
//    val stds = CollectUtils.newArrayList()
//    for (take <- takes) {
//      stds.add(take.getStd)
//    }
//    val query = OqlBuilder.from(classOf[CourseGrade], "grade").where(new Condition("grade.lesson=:lesson", 
//      lesson))
//    if (stds.size > 0) query.where(new Condition("grade.std in(:stds)", stds)) else query.where(new Condition("grade.std is null"))
//    val grades = entityDao.search(query)
//    val existedGradeTypes = CollectUtils.newHashSet()
//    for (grade <- grades; eg <- grade.getExamGrades) existedGradeTypes.add(eg.getGradeType)
//    var orderBy = get("orderBy")
//    if (Strings.isEmpty(orderBy)) {
//      orderBy = "std.code"
//    } else {
//      if (orderBy.startsWith("courseGrade.")) orderBy = Strings.substringAfter(orderBy, "courseGrade.")
//    }
//    val orders = Order.parse(orderBy)
//    val makeupTypes = baseCodeService.getCodes(classOf[GradeType], GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID)
//    existedGradeTypes.addAll(makeupTypes)
//    val gradeTypes = CollectUtils.newArrayList(existedGradeTypes)
//    Collections.sort(gradeTypes, new PropertyComparator("code"))
//    val order = orders.get(0).asInstanceOf[Order]
//    Collections.sort(grades, new CourseGradeComparator(order.getProperty, order.isAscending, gradeTypes))
//    put("grades", grades)
//    put("lesson", lesson)
//    put("gradeTypes", gradeTypes)
//    put("NORMAL", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
//    forward("info")
//  }
//
//  override def save(): String = super.save()
}
