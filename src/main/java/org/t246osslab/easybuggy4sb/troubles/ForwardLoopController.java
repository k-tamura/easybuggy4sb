package org.t246osslab.easybuggy4sb.troubles;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardLoopController {

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/forwardloop")
    public void process(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        RequestDispatcher dispatch = req.getRequestDispatcher("/forwardloop");
        dispatch.forward(req, res);
    }
}