<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test HTML Form Action Reference</title>
</head>
<body>
    <!-- Test form with separate namespace and action attributes -->
    <form name="fm" action="processCodeInputCon<caret>tinue.do" namespace="/common" method="post">
        <input type="submit" value="Submit" />
    </form>
</body>
</html>