package org.openurp.edu.eams.teach.grade.setting.web.action

import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.model.GradeRateItem
import org.openurp.edu.eams.web.action.common.ProjectSupportAction

import scala.collection.JavaConversions._

class RateAction extends ProjectSupportAction {

  protected override def getEntityName(): String = classOf[GradeRateConfig].getName

  def search(): String = {
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "gradeRateConfig")
    builder.where("gradeRateConfig.project=:project", getProject)
    populateConditions(builder)
    builder.limit(getPageLimit)
    builder.orderBy(Order.parse(get("orderBy")))
    put("gradeRateConfigs", entityDao.search(builder))
    forward()
  }

  def addConfig(): String = {
    val builder = OqlBuilder.from(classOf[ScoreMarkStyle], "markStyle")
    populateConditions(builder)
    val project = getProject
    builder.where("not exists(from " + classOf[GradeRateConfig].getName + 
      " cfg where cfg.scoreMarkStyle=markStyle and cfg.project=:project)", project)
    builder.orderBy("markStyle.code")
    put("markStyles", entityDao.search(builder))
    put("project", project)
    forward()
  }

  def save(): String = {
    var gradeRateConfig = populateEntity(classOf[GradeRateConfig], "gradeRateConfig")
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "gradeRate")
    builder.where("gradeRate.project =:project", gradeRateConfig.getProject)
    builder.where("gradeRate.scoreMarkStyle =:scoreMarkStyle", gradeRateConfig.getScoreMarkStyle)
    val gradeRateConfigs = entityDao.search(builder)
    if (CollectUtils.isNotEmpty(gradeRateConfigs)) {
      gradeRateConfig = gradeRateConfigs.get(0)
      gradeRateConfig.setPassScore(gradeRateConfig.getPassScore)
    }
    entityDao.saveOrUpdate(gradeRateConfig)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    val configs = entityDao.get(classOf[GradeRateConfig], Strings.splitToLong(get("gradeRateConfig.ids")))
    entityDao.remove(configs)
    redirect("search", "info.action.success")
  }

  def setting(): String = {
    put("gradeRateConfig", entityDao.get(classOf[GradeRateConfig], getLong("gradeRateConfig.id")))
    forward()
  }

  def saveConfigSettng(): String = {
    val gradeRateConfig = entityDao.get(classOf[GradeRateConfig], getLong("gradeRateConfig.id")).asInstanceOf[GradeRateConfig]
    val configItemIds = Strings.splitToLong(get("configItemIds"))
    if (null == configItemIds || configItemIds.length == 0) {
      var converters = gradeRateConfig.getItems
      if (CollectUtils.isEmpty(converters)) {
        converters = CollectUtils.newArrayList()
      }
      val configItem = populateEntity(classOf[GradeRateItem], "configItem")
      gradeRateConfig.getItems.add(configItem)
      configItem.setConfig(gradeRateConfig)
      try {
        entityDao.saveOrUpdate(gradeRateConfig)
      } catch {
        case e: Exception => return redirect("setting", "info.action.failure", "&gradeRateConfig.id=" + gradeRateConfig.getId)
      }
      redirect("setting", "info.action.success", "&gradeRateConfig.id=" + gradeRateConfig.getId)
    } else {
      val itemMap = CollectUtils.newHashMap()
      for (configItem <- gradeRateConfig.getItems) {
        itemMap.put(configItem.getId, configItem)
      }
      for (i <- 0 until configItemIds.length) {
        val configItem = itemMap.get(configItemIds(i))
        configItem.setGrade(get("scoreName" + configItemIds(i)))
        configItem.setMaxScore(getFloat("maxScore" + configItemIds(i)))
        configItem.setMinScore(getFloat("minScore" + configItemIds(i)))
        configItem.setDefaultScore(getFloat("defaultScore" + configItemIds(i)))
        configItem.setGpExp(get("gpExp" + configItemIds(i)))
      }
      try {
        entityDao.saveOrUpdate(gradeRateConfig)
      } catch {
        case e: Exception => return redirect("search", "info.action.failure")
      }
      redirect("search", "info.action.success")
    }
  }

  def removeConfigSettng(): String = {
    entityDao.remove(entityDao.get(classOf[GradeRateItem], Strings.splitToLong(get("configItemIds"))))
    redirect("setting", "info.action.success")
  }

  def info(): String = {
    put("gradeRateConfig", entityDao.get(classOf[GradeRateConfig], getLongId("gradeRateConfig")))
    forward()
  }
}
