<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello world</title>
</head>
<body>
Hello ${testName} !
<#--<table>-->
<#--    <tr>-->
<#--        <td>序号</td>-->
<#--        <td>姓名</td>-->
<#--        <td>年龄</td>-->
<#--        <td>钱包</td>-->
<#--    </tr>-->
<#--    <#list stus as stu>-->
<#--        <tr>-->
<#--            <td>${stu_index+1}</td>-->
<#--            <td>${stu.name}</td>-->
<#--            <td>${stu.age}</td>-->
<#--            <td>${stu.money}</td>-->
<#--        </tr>-->
<#--    </#list>-->
<#--</table>-->
<#--输出stu1学生信息<br>-->
<#--姓名: ${stuMap["stu1"].name} <br>-->
<#--年龄: ${stuMap["stu1"].age} <br>-->
<#--钱包: ${stuMap["stu1"].money} <br>-->
<#--输出stu2学生信息<br>-->
<#--姓名: ${stuMap.stu2.name} <br>-->
<#--年龄: ${stuMap.stu2.age} <br>-->
<#--钱包: ${stuMap.stu1.money} <br>-->

遍历map
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#if stus??>
    <#list stuMap?keys as key>
        <tr>
            <td <#if key_index==0>style="background-color: red" </#if>>${key_index+1}</td>
            <td>${(stuMap[key].name)!""}</td>
            <td>${(stuMap[key].age)!""}</td>
        </tr>
    </#list>
    </#if>
</table>

request属性值: ${Request["attr1"]} <br>
session属性值: ${Session["session1"]} <br>
contextPath: ${rc.contextPath} <br>
url: ${rc.requestUri}<br>
url请参数值: ${RequestParameters["param1"]!""}<br>
</body>
</html>