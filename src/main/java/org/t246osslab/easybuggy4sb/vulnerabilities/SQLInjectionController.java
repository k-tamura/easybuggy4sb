package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class SQLInjectionController extends AbstractController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/sqlijc")
    public ModelAndView process(@RequestParam(value = "mail", required = false) String mail,
            @RequestParam(value = "password", required = false) String password, ModelAndView mav,
            HttpServletRequest req, Locale locale) {
	    setViewAndCommonObjects(mav, locale, "sqlijc");
		mail = StringUtils.trim(mail);
		password = StringUtils.trim(password);
		if (!StringUtils.isBlank(mail) && !StringUtils.isBlank(password) && password.length() >= 8) {
			try {
				List<User> users = selectUsers(mail, password);
				if (users == null || users.isEmpty()) {
					mav.addObject("errmsg", msg.getMessage("msg.error.user.not.exist", null, locale));
				} else {
					mav.addObject("userList", users);
				}
            } catch (DataAccessException se) {
                log.error("DataAccessException occurs: ", se);
                mav.addObject("errmsg", msg.getMessage("msg.db.access.error.occur", null, locale));
            }
		} else {
            if (req.getMethod().equalsIgnoreCase("POST")) {
                mav.addObject("errmsg", msg.getMessage("msg.warn.enter.mail.and.passwd", null, locale));
            }
        }
		return mav;
	}

	private List<User> selectUsers(String mail, String password) {

		return jdbcTemplate.query("SELECT mail, secret FROM users " +
				"WHERE ispublic = 'true' AND mail='" + mail
				+ "' AND password='" + password + "'", new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User user = new User();
                        user.setMail(rs.getString("mail"));
                        user.setSecret(rs.getString("secret"));
                        return user;
                    }
				});
	}
}
