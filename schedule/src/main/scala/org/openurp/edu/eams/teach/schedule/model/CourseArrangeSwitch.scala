package org.openurp.edu.eams.teach.schedule.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-1830248177687127758L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch")
class CourseArrangeSwitch extends LongIdObject {

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @BooleanBeanProperty
  var published: Boolean = _

  def this(semester: Semester) {
    this()
    this.published = false
    this.semester = semester
  }

  def this(semester: Semester, project: Project) {
    this()
    this.published = false
    this.semester = semester
    this.project = project
  }
}
