package org.openurp.edu.eams.teach.schedule.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import CollisionResource._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object CollisionResource {

  object ResourceType extends Enumeration {

    val ADMINCLASS = new ResourceType()

    val CLASSROOM = new ResourceType()

    val TEACHER = new ResourceType()

    val PROGRAM = new ResourceType()

    class ResourceType extends Val

    implicit def convertValue(v: Value): ResourceType = v.asInstanceOf[ResourceType]
  }
}

@SerialVersionUID(1L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CollisionResource")
class CollisionResource extends LongIdObject() {

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var lesson: Lesson = _

  @NaturalId
  @BeanProperty
  var resourceId: String = _

  @NaturalId
  @Enumerated(value = EnumType.STRING)
  @BeanProperty
  var resourceType: ResourceType = _

  def this(semester: Semester, 
      lesson: Lesson, 
      resourceId: String, 
      resourceType: ResourceType) {
    super()
    this.semester = semester
    this.lesson = lesson
    this.resourceId = resourceId
    this.resourceType = resourceType
  }
}
