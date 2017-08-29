package org.t246osslab.easybuggy4sb.controller;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ServerInfoController {

    @RequestMapping(value = "/serverinfo")
    public void serverinfo(HttpServletResponse res) throws IOException {
        Properties properties = System.getProperties();
        for (Object key : properties.keySet()) {
            Object value = properties.get(key);
            res.getWriter().write("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
        }
    }
}