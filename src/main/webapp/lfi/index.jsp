<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="session" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="messages" />
<!DOCTYPE html>
<html>
<head>
    <title><fmt:message key="title.design.test.page" /></title>
    <link rel="icon" type="image/vnd.microsoft.icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    <c:catch var="ex">
    	<c:if test="${param.template != null && !fn:startsWith(param.template,'http')}">
            <c:import url="<%=request.getParameter(\"template\")%>" />
        </c:if>
    </c:catch>
</head>
<body style="margin-left: 20px; margin-right: 20px;">
<table style="width: 100%;">
    <tr>
        <td>
            <h2>
                <span class="glyphicon glyphicon-globe"></span>&nbsp;
                <fmt:message key="title.design.test.page" />
            </h2>
        </td>
        <td align="right"><a href="${pageContext.request.contextPath}/"><fmt:message key="label.go.to.main" /></a></td>
    </tr>
</table>
<hr style="margin-top: 0" />
<header>
    <img src="${pageContext.request.contextPath}/images/easybuggy.png">
</header>
<hr style="margin-top: 10px" />
<p>
    <fmt:message key="description.design.page" />
</p>
<ul>
    <li><p>
        <a href="index.jsp"><fmt:message key="style.name.nonstyle" /></a>:
        <fmt:message key="style.description.nonstyle" />
    </p></li>
    <li><p>
        <a href="index.jsp?template=style_bootstrap.html"><fmt:message key="style.name.bootstrap" /></a>:
        <fmt:message key="style.description.bootstrap" />
    </p></li>
    <li><p>
        <a href="index.jsp?template=style_google_mdl.html"><fmt:message key="style.name.google.mdl" /></a>:
        <fmt:message key="style.description.google.mdl" />
    </p></li>
    <li><p>
        <a href="index.jsp?template=style_materialize.html"><fmt:message key="style.name.materialize" /></a>:
        <fmt:message key="style.description.materialize" />
    </p></li>
</ul>
<br>
<div class="alert alert-info" role="alert">
    <span class="glyphicon glyphicon-info-sign"></span>&nbsp;
    <fmt:message key="msg.note.local.file.inclusion" />
</div>
<hr>
<footer>
    <img src="/images/easybuggyL.png">Copyright &copy; 2016-2025 T246 OSS Lab, all rights reserved.
</footer>
</body>
</html>
