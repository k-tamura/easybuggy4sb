<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.File"%>
<% displayImage(response); %>
<%!
  public void displayImage(HttpServletResponse resp) throws Exception {

    ServletContext sc = getServletContext();
    String filename = sc.getRealPath("img/avatar_woman.png");
    String mimeType = "image/png";
    resp.setContentType(mimeType);
    File file = new File(filename);
    resp.setContentLength((int)file.length());
    FileInputStream in = new FileInputStream(file);
    OutputStream out = resp.getOutputStream();
    byte[] buf = new byte[1024];
    int count = 0;
    while ((count = in.read(buf)) >= 0) {
      out.write(buf, 0, count);
    }
    in .close();
    out.close();
  }
%>