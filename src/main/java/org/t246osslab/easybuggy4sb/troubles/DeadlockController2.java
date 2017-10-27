package org.t246osslab.easybuggy4sb.troubles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class DeadlockController2 extends AbstractController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager txMgr;

    @RequestMapping(value = "/deadlock2")
    public ModelAndView process(HttpServletRequest req, HttpSession ses, ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "deadlock2");
        // Overwrite title (because title is the same as xee page)
        mav.addObject("title", msg.getMessage("title.xee.page", null, locale));
        List<User> users = null;
        String order = getOrder(req);
        if ("POST".equals(req.getMethod())) {
            users = new ArrayList<>();
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
            users = selectUsers(order, locale, mav);
        }
        mav.addObject("userList", users);
        mav.addObject("order", order);
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

    private List<User> selectUsers(String order, Locale locale, ModelAndView mav) {
        List<User> users = null;
        try {
            users = jdbcTemplate.query("select * from users where ispublic = 'true' order by id "
                    + ("desc".equals(order) ? "desc" : "asc"), new RowMapper<User>() {
                        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                            User user = new User();
                            user.setUserId(rs.getString("id"));
                            user.setName(rs.getString("name"));
                            user.setPhone(rs.getString("phone"));
                            user.setMail(rs.getString("mail"));
                            return user;
                        }
                    });
        } catch (DataAccessException e) {
            mav.addObject("errmsg",
                    msg.getMessage("msg.db.access.error.occur", new String[] { e.getMessage() }, null, locale));
            log.error("DataAccessException occurs: ", e);
        } catch (Exception e) {
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
            log.error("Exception occurs: ", e);
        }
        return users;
    }

    private void updateUsers(List<User> users, Locale locale, ModelAndView mav) {
        DefaultTransactionDefinition dtDef = new DefaultTransactionDefinition();

        TransactionStatus trnStatus = txMgr.getTransaction(dtDef);
        int executeUpdate = 0;
        try {
            for (User user : users) {
                executeUpdate = executeUpdate
                        + jdbcTemplate.update("Update users set name = ?, phone = ?, mail = ? where id = ?",
                                user.getName(), user.getPhone(), user.getMail(), user.getUserId());
                log.info(user.getUserId() + " is updated.");
                Thread.sleep(500);
            }
            txMgr.commit(trnStatus);
            mav.addObject("msg", msg.getMessage("msg.update.records", new Object[] { executeUpdate }, null, locale));
        } catch (DeadlockLoserDataAccessException e) {
            txMgr.rollback(trnStatus);
            mav.addObject("errmsg", msg.getMessage("msg.deadlock.occurs", null, locale));
            log.error("DeadlockLoserDataAccessException occurs: ", e);
        } catch (DataAccessException e) {
            txMgr.rollback(trnStatus);
            mav.addObject("errmsg",
                    msg.getMessage("msg.db.access.error.occur", new String[] { e.getMessage() }, null, locale));
            log.error("DataAccessException occurs: ", e);
        } catch (Exception e) {
            txMgr.rollback(trnStatus);
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
            log.error("Exception occurs: ", e);
        }
    }
}
