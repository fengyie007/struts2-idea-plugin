<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Test ActionLink Namespace Separation</title>
</head>
<body>
    <!-- Test case 1: Action link with separate namespace and action -->
    <a href="processCodeInputContinue.action" namespace="/common">Process Code Input</a>
    
    <!-- Test case 2: Action link with namespace in parent tag -->
    <div namespace="/test">
        <a href="testAction.action">Test Action</a>
    </div>
    
    <!-- Test case 3: Traditional action link with namespace in path -->
    <a href="/common/processCodeInputContinue.action">Traditional Link</a>
    
    <!-- Test case 4: Struts URL tag with separate namespace -->
    <s:url action="urlAction" namespace="/url" />
</body>
</html>