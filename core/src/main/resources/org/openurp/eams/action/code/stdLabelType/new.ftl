[#ftl]
[@b.head/]
[@b.toolbar title="新建学生分类标签类型"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
    [@b.textfield name="stdLabelType.code" label="代码" value="${stdLabelType.code!}" required="true" maxlength="20"/]
    [@b.textfield name="stdLabelType.name" label="名称" value="${stdLabelType.name!}" required="true" maxlength="20"/]
    [@b.textfield name="stdLabelType.enName" label="英文名" value="${stdLabelType.enName!}" maxlength="100"/]
    [@b.startend label="生效失效时间" 
      name="stdLabelType.beginOn,stdLabelType.endOn" required="false,false" 
      start=stdLabelType.beginOn end=stdLabelType.endOn format="date"/]
    [@b.textfield name="stdLabelType.remark" label="备注" value="${stdLabelType.remark!}" maxlength="30"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]