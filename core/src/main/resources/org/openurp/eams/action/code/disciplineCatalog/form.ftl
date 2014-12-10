[#ftl]
[@b.head/]
[@b.toolbar title="修改学科目录"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!update?id=${disciplineCatalog.id}" theme="list"]
    [@b.textfield name="disciplineCatalog.code" label="代码" value="${disciplineCatalog.code!}" required="true" maxlength="20"/]
    [@b.textfield name="disciplineCatalog.name" label="名称" value="${disciplineCatalog.name!}" required="true" maxlength="20"/]
    [@b.textfield name="disciplineCatalog.enName" label="英文名" value="${disciplineCatalog.enName!}" maxlength="100"/]
    [@b.startend label="生效失效时间" 
      name="disciplineCatalog.beginOn,disciplineCatalog.endOn" required="false,false" 
      start=disciplineCatalog.beginOn end=disciplineCatalog.endOn format="date"/]
    [@b.textfield name="disciplineCatalog.remark" label="备注" value="${disciplineCatalog.remark!}" maxlength="3"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]