package org.t246osslab.easybuggy4sb.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultLogoutController {

	@Autowired
    MessageSource msg;
    
	@RequestMapping(value = "/logout")
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        HttpSession session = req.getSession(true);
        session.invalidate();
        res.sendRedirect("/");
    }
}
