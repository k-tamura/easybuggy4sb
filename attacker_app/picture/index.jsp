<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.File"%>
<%
ServletContext sc = getServletContext();
File file = new File(sc.getRealPath("img/avatar_attacker.png"));
try (FileInputStream fis = new FileInputStream(file);OutputStream os = response.getOutputStream();){
    response.setContentType("image/png");
    response.setContentLength((int) file.length());
    byte[] buf = new byte[1024];
    int count = 0;
    while ((count = fis.read(buf)) >= 0) {
        os.write(buf, 0, count);
    }
}
%>
