package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.dao.DBClient;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class SQLInjectionController {

    private static final Logger log = LoggerFactory.getLogger(SQLInjectionController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/sqlijc")
    public ModelAndView process(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "password", required = false) String password, ModelAndView mav, Locale locale) {
        mav.setViewName("sqlijc");
        mav.addObject("title", msg.getMessage("title.sql.injection.page", null, locale));
        name = StringUtils.trim(name);
        name = StringUtils.trim(password);
        if (!StringUtils.isBlank(name) && !StringUtils.isBlank(password) && password.length() >= 8) {
            try {
                List<User> users = selectUsers(name, password);
                if (users == null || users.isEmpty()) {
                    mav.addObject("errmsg", msg.getMessage("msg.error.user.not.exist", null, locale));
                } else {
                    mav.addObject("userList", users);
                }
            } catch (SQLException se) {
                log.error("SQLException occurs: ", se);
                mav.addObject("errmsg", msg.getMessage("msg.db.access.error.occur", null, locale));
            }
        } else {
            mav.addObject("errmsg", msg.getMessage("msg.warn.enter.name.and.passwd", null, locale));
        }
        return mav;
    }

    private List<User> selectUsers(String name, String password) throws SQLException {

        ArrayList<User> userList = new ArrayList<>();
        try (Connection conn = DBClient.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name, secret FROM users WHERE ispublic = 'true' AND name='"
                        + name + "' AND password='" + password + "'")) {
            while (rs.next()) {
                User user = new User();
                user.setName(rs.getString("name"));
                user.setSecret(rs.getString("secret"));
                userList.add(user);
            }
        }
        return userList;
    }
}
