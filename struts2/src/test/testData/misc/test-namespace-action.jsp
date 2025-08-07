<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Test Namespace Action Separation</title>
</head>
<body>
    <!-- Test case 1: HTML form with separate namespace and action attributes -->
    <form name="fm" action="processCodeInputContinue.do" namespace="/common" method="post">
        <input type="submit" value="Submit" />
    </form>
    
    <!-- Test case 2: Struts form tag with separate namespace and action attributes -->
    <s:form name="strutsForm" action="testAction" namespace="/test">
        <s:submit value="Submit Struts Form" />
    </s:form>
    
    <!-- Test case 3: Struts URL tag with separate namespace and action attributes -->
    <s:url action="urlAction" namespace="/url" />
    
    <!-- Test case 4: Nested tags with namespace inheritance -->
    <div namespace="/parent">
        <s:form action="childAction">
            <s:submit value="Child Action" />
        </s:form>
    </div>
</body>
</html