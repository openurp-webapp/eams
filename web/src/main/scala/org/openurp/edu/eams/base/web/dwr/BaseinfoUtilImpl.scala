package org.openurp.edu.eams.base.web.dwr

import org.beangle.commons.dao.impl.BaseServiceImpl



class BaseinfoUtilImpl extends BaseServiceImpl {

  def checkCodeIfExists(className: String, keyName: String, code: String): Boolean = {
    entityDao.exist(className, keyName, code)
  }
}
