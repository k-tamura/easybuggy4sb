package org.t246osslab.easybuggy4sb.troubles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class DeadlockController2 {

    private static final Logger log = LoggerFactory.getLogger(DeadlockController2.class);

    @Autowired
    MessageSource msg;

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	private PlatformTransactionManager txMgr;
	
    @RequestMapping(value = "/deadlock2")
    public ModelAndView process(HttpServletRequest req, HttpSession ses, ModelAndView mav, Locale locale) {
        mav.setViewName("deadlock2");
        mav.addObject("title", msg.getMessage("title.xxe", null, locale));

        List<User> users = null;
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

    private List<User> selectUsers(String order) {

		return jdbcTemplate.query("select * from users where ispublic = 'true' order by id "
				+ ("desc".equals(order) ? "desc" : "asc"), (rs, i) -> {
	                User user = new User();
	                user.setUserId(rs.getString("id"));
	                user.setName(rs.getString("name"));
	                user.setPhone(rs.getString("phone"));
	                user.setMail(rs.getString("mail"));
					return user;
				});
    }

    @Transactional
    private void updateUsers(List<User> users, Locale locale, ModelAndView mav) {
		DefaultTransactionDefinition dtDef = new DefaultTransactionDefinition();

		TransactionStatus trnStatus = txMgr.getTransaction(dtDef);
        int executeUpdate = 0;
        try {
            for (User user : users) {
            	executeUpdate = executeUpdate + jdbcTemplate.update("Update users set name = ?, phone = ?, mail = ? where id = ?", user.getName(),
						user.getPhone(), user.getMail(), user.getUserId());
                log.info(user.getUserId() +" is updated.");
                Thread.sleep(500);
            }
            txMgr.commit(trnStatus);
            mav.addObject("msg", msg.getMessage("msg.update.records", new Object[] { executeUpdate }, null, locale));
		} catch (DeadlockLoserDataAccessException e) {
			mav.addObject("errmsg", msg.getMessage("msg.deadlock.occurs", null, locale));
			log.error("DeadlockLoserDataAccessException occurs: ", e);
		} catch (DataAccessException e) {
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
			log.error("DataAccessException occurs: ", e);
		} catch (Exception e) {
			txMgr.rollback(trnStatus);
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
			log.error("Exception occurs: ", e);
		}
    }
}
