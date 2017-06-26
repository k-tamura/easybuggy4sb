package org.t246osslab.easybuggy4sb.troubles;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.dao.DBClient;
import org.t246osslab.easybuggy4sb.core.model.User;
import org.t246osslab.easybuggy4sb.core.utils.ApplicationUtils;

@Controller
public class DBConnectionLeakController {

    private static final Logger log = LoggerFactory.getLogger(DBConnectionLeakController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/dbconnectionleak")
    public ModelAndView process(ModelAndView mav, Locale locale) {
        mav.setViewName("dbconnectionleak");
        mav.addObject("title", msg.getMessage("title.user.list", null, locale));
        try {
            if (StringUtils.isBlank(ApplicationUtils.getDatabaseURL())
                    || ApplicationUtils.getDatabaseURL().startsWith("jdbc:derby:memory:")) {
                mav.addObject("note", msg.getMessage("msg.note.not.use.ext.db", null, locale));
                return mav;
            } else {
                mav.addObject("note", msg.getMessage("msg.note.db.connection.leak.occur", null, locale));
            }

            try {
                List<User> users = selectUsers();
                if (users == null || users.isEmpty()) {
                    mav.addObject("errmsg", msg.getMessage("msg.error.user.not.exist", null, locale));
                } else {
                    mav.addObject("userList", users);
                }
            } catch (SQLException se) {
                log.error("SQLException occurs: ", se);
                mav.addObject("errmsg", msg.getMessage("msg.db.access.error.occur", null, locale));
            }
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
        }
        return mav;
    }

    private List<User> selectUsers() throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<User> userList = new ArrayList<>();
        conn = DBClient.getConnection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("select id, name, phone, mail from users where ispublic = 'true'");
        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setMail(rs.getString("mail"));
            userList.add(user);
        }
        return userList;
    }
}
