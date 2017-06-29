package org.t246osslab.easybuggy4sb.troubles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.dao.DBClient;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class DeadlockController2 {

    private static final Logger log = LoggerFactory.getLogger(DeadlockController2.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/deadlock2")
    public ModelAndView process(HttpServletRequest req, HttpSession ses, ModelAndView mav, Locale locale) {
        mav.setViewName("deadlock2");
        mav.addObject("title", msg.getMessage("title.xxe", null, locale));

        ArrayList<User> users = null;
        try {
            String order = getOrder(req);
            if ("POST".equals(req.getMethod())) {
                users = new ArrayList<User>();
                for (int j = 0;; j++) {
                    String uid = req.getParameter("uid_" + (j + 1));
                    if (uid == null) {
                        break;
                    }
                    User user = new User();
                    user.setUserId(uid);
                    user.setName(req.getParameter(uid + "_name"));
                    user.setPhone(req.getParameter(uid + "_phone"));
                    user.setMail(req.getParameter(uid + "_mail"));
                    users.add(user);
                }
                updateUsers(users, locale, mav);
            } else {
                users = selectUsers(order);
            }
            mav.addObject("userList", users);
            mav.addObject("order", order);

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
        }
        return mav;
    }

    private String getOrder(HttpServletRequest req) {
        String order = req.getParameter("order");
        if ("asc".equals(order)) {
            order = "desc";
        } else {
            order = "asc";
        }
        return order;
    }

    private ArrayList<User> selectUsers(String order) {

        ArrayList<User> users = new ArrayList<User>();
		try (Connection conn = DBClient.getConnection();
				Statement stmt = createStatement(conn);
				ResultSet rs = stmt.executeQuery("select * from users where ispublic = 'true' order by id "
						+ ("desc".equals(order) ? "desc" : "asc"));) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setMail(rs.getString("mail"));
                users.add(user);
            }
        } catch (SQLException e) {
            log.error("SQLException occurs: ", e);
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        }
        return users;
    }

	private Statement createStatement(Connection conn) throws SQLException {
        conn.setAutoCommit(true);
		return conn.createStatement();
	}

    private void updateUsers(ArrayList<User> users, Locale locale, ModelAndView mav) {

        int executeUpdate = 0;
        try (Connection conn = DBClient.getConnection();PreparedStatement stmt = preparedStatement(conn)) {
            
            for (User user : users) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getPhone());
                stmt.setString(3, user.getMail());
                stmt.setString(4, user.getUserId());
                executeUpdate = executeUpdate + stmt.executeUpdate();
                log.info(user.getUserId() +" is updated.");
                Thread.sleep(500);
            }
            conn.commit();
            mav.addObject("msg", msg.getMessage("msg.update.records", new Object[] { executeUpdate }, null, locale));

		} catch (SQLTransactionRollbackException e) {
			mav.addObject("errmsg", msg.getMessage("msg.deadlock.occurs", null, locale));
			log.error("SQLTransactionRollbackException occurs: ", e);
		} catch (SQLException e) {
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
			log.error("SQLException occurs: ", e);
		} catch (Exception e) {
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
			log.error("Exception occurs: ", e);
		}
    }

	private PreparedStatement preparedStatement(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
		return conn.prepareStatement("Update users set name = ?, phone = ?, mail = ? where id = ?");
	}
}
