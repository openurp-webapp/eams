package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Set
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.AdminclassService

import scala.collection.JavaConversions._

class AdminclassServiceImpl extends BaseServiceImpl with AdminclassService {

  def getAdminclass(id: java.lang.Integer): Adminclass = {
    entityDao.get(classOf[Adminclass], id).asInstanceOf[Adminclass]
  }

  def getAdminclass(code: String): Adminclass = {
    val query = OqlBuilder.from(classOf[Adminclass], "adminclass")
    query.where("adminclass.code=:code", code)
    val rs = entityDao.search(query)
    if (rs.isEmpty) null else rs.get(0).asInstanceOf[Adminclass]
  }

  def removeAdminclass(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Adminclass], id))
  }

  def saveOrUpdate(adminclass: Adminclass) {
    if (null == adminclass.getCreatedAt) {
      adminclass.setCreatedAt(new Date(System.currentTimeMillis()))
    }
    if (!adminclass.isPersisted) {
      adminclass.setCreatedAt(new Date(System.currentTimeMillis()))
    }
    adminclass.setUpdatedAt(new Date(System.currentTimeMillis()))
    entityDao.saveOrUpdate(adminclass)
  }

  def updateActualStdCount(adminclassId: java.lang.Integer): Int = {
    val stdCount = 0
    val updateHQL = "update Adminclass cls set cls.actualStdCount=(\n" + 
      "select count(std.id) from Adminclass class1 join class1.students std where class1.id=cls.id and std.inSchool=1\n" + 
      ") where cls.id=:id"
    val params = new HashMap()
    params.put("id", adminclassId)
    try {
      entityDao.executeUpdate(updateHQL, params)
    } catch {
      case e: RuntimeException => {
        logger.info("execproduct is failed" + "in update_classactualstdcount" + 
          Throwables.getStackTrace(e))
        throw e
      }
    }
    stdCount
  }

  def updateStdCount(adminclassId: java.lang.Integer): Int = {
    val stdCount = 0
    val updateHQL = "update Adminclass cls set cls.stdCount=(\n" + 
      "select count(std.id) from Adminclass class1 join class1.students std where class1.id=cls.id and std.active=1\n" + 
      ") where cls.id=:id"
    val params = new HashMap()
    params.put("id", adminclassId)
    try {
      entityDao.executeUpdate(updateHQL, params)
    } catch {
      case e: RuntimeException => {
        logger.info("execproduct is failed" + "in update_classstdcount" + 
          Throwables.getStackTrace(e))
        throw e
      }
    }
    stdCount
  }

  def batchUpdateStdCountOfClass(adminclassIdSeq: String) {
    val adminclassIds = Strings.transformToInt(Strings.split(adminclassIdSeq))
    if (null != adminclassIds) {
      for (i <- 0 until adminclassIds.length) {
        updateActualStdCount(adminclassIds(i))
        updateStdCount(adminclassIds(i))
      }
    }
  }

  def batchUpdateStdCountOfClass(adminclassIds: Array[Integer]) {
    if (null != adminclassIds) {
      for (i <- 0 until adminclassIds.length) {
        updateActualStdCount(adminclassIds(i))
        updateStdCount(adminclassIds(i))
      }
    }
  }

  def batchAddStudentClass(students: List[_], adminclasses: List[_]) {
    var iterator = adminclasses.iterator()
    while (iterator.hasNext) {
      val adminclass = iterator.next().asInstanceOf[Adminclass]
      val studentSet = adminclass.getStudents
      var iter = students.iterator()
      while (iter.hasNext) {
        val student = iter.next().asInstanceOf[Student]
        if (!studentSet.contains(student)) {
          studentSet.add(student)
        }
      }
    }
    entityDao.saveOrUpdate(students)
    entityDao.saveOrUpdate(adminclasses)
  }

  def batchRemoveStudentClass(students: List[_], adminclasses: List[_]) {
    var iterator = adminclasses.iterator()
    while (iterator.hasNext) {
      val adminclass = iterator.next().asInstanceOf[Adminclass]
      val studentSet = adminclass.getStudents
      var iter = students.iterator()
      while (iter.hasNext) {
        val student = iter.next().asInstanceOf[Student]
        if (studentSet.contains(student)) {
          studentSet.remove(student)
        }
      }
    }
    entityDao.saveOrUpdate(students)
    entityDao.saveOrUpdate(adminclasses)
  }

  def updateStudentAdminclass(std: Student, adminclasses: Collection[_], project: Project) {
    val orig = EntityUtils.extractIds(adminclasses)
    val dest = EntityUtils.extractIds(Collections.singleton(std.getAdminclass))
    val addClassList = CollectUtils.subtract(orig, dest)
    val subClassList = CollectUtils.subtract(dest, orig)
    batchRemoveStudentClass(Collections.singletonList(std), entityDao.get(classOf[Adminclass], "id", 
      subClassList.toArray()))
    batchAddStudentClass(Collections.singletonList(std), entityDao.get(classOf[Adminclass], "id", addClassList.toArray()))
  }
}
