<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.File"%>
<% displayImage(response); %>
<%!
	public void displayImage(HttpServletResponse resp) throws Exception {

		ServletContext sc = getServletContext();
		File file = new File(sc.getRealPath("img/avatar_woman.png"));
		try (FileInputStream in = new FileInputStream(file);OutputStream out = resp.getOutputStream();){
			resp.setContentType("image/png");
			resp.setContentLength((int) file.length());
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
		}
	}
%>