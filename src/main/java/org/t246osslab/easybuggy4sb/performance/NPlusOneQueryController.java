package org.t246osslab.easybuggy4sb.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class NPlusOneQueryController extends AbstractController {

    private static final int NUM_OF_CUSTOMERS = 500;
    private static final int NUM_OF_ORDERS = 10000;

    private static final String[] FIRST_NAMES = { "Mark", "David", "Peter", "James", "Benjamin", "Anthony",
            "Chris", "Daniel", "Edward", "George", "Henry", "Jack", "Kevin", "Laura", "Mary", "Nancy", "Olivia",
            "Paul", "Rachel", "Sarah" };
    private static final String[] LAST_NAMES = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris",
            "Martin", "Thompson", "Young", "Allen", "King", "Wright", "Scott", "Green", "Baker" };
    private static final String[] PRODUCTS = { "Notebook", "Ballpoint Pen", "Mechanical Pencil", "Eraser",
            "Stapler", "Scissors", "Ruler", "Glue Stick", "Sticky Notes", "Highlighter" };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager txMgr;

    @RequestMapping(value = "/nplusone")
    public ModelAndView process(@RequestParam(value = "number", required = false) String strNumber,
            ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "nplusone");
        int number = NumberUtils.toInt(strNumber, -1);
        if (number < 1 || number > NUM_OF_ORDERS) {
            mav.addObject("msg", msg.getMessage("msg.enter.number.of.orders", new Object[] { NUM_OF_ORDERS }, locale));
            return mav;
        }
        mav.addObject("number", number);
        try {
            initData();
            long start = System.currentTimeMillis();
            int queryCount = 0;

            // Fetch the orders (1 query)
            List<Order> orders = jdbcTemplate.query(
                    "select id, customer_id, product, amount from orders where id <= ? order by id",
                    (rs, rowNum) -> {
                        Order order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setCustomerId(rs.getInt("customer_id"));
                        order.setProduct(rs.getString("product"));
                        order.setAmount(rs.getInt("amount"));
                        return order;
                    }, number);
            queryCount++;

            // Fetch the customer name for each order (N queries)
            for (Order order : orders) {
                String customerName = jdbcTemplate.queryForObject("select name from customers where id = ?",
                        String.class, order.getCustomerId());
                queryCount++;
                order.setCustomerName(customerName);
            }

            long elapsed = System.currentTimeMillis() - start;
            mav.addObject("orders", orders);
            mav.addObject("msg", msg.getMessage("msg.show.orders",
                    new Object[] { orders.size(), queryCount, elapsed }, locale));
        } catch (DataAccessException e) {
            mav.addObject("errmsg", msg.getMessage("msg.db.access.error.occur", null, locale));
            mav.addObject("detailmsg", e.getMessage());
            log.error("DataAccessException occurs: ", e);
        }
        return mav;
    }

    private synchronized void initData() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from customers", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        List<Object[]> customers = new ArrayList<>();
        for (int i = 0; i < NUM_OF_CUSTOMERS; i++) {
            String name = FIRST_NAMES[i % FIRST_NAMES.length] + " " + LAST_NAMES[i / FIRST_NAMES.length];
            customers.add(new Object[] { i + 1, name });
        }

        List<Object[]> orders = new ArrayList<>();
        for (int i = 0; i < NUM_OF_ORDERS; i++) {
            int customerId = (i * 7) % NUM_OF_CUSTOMERS + 1;
            String product = PRODUCTS[i % PRODUCTS.length];
            int amount = ((i * 37) % 20 + 1) * 100;
            orders.add(new Object[] { i + 1, customerId, product, amount });
        }
        // Insert all rows in one transaction, 500 rows per statement, to reduce the number of round trips
        new TransactionTemplate(txMgr).execute(status -> {
            insertRows("insert into customers (id, name) values ", "(?, ?)", customers);
            insertRows("insert into orders (id, customer_id, product, amount) values ", "(?, ?, ?, ?)", orders);
            return null;
        });
        log.info("Inserted {} customers and {} orders.", NUM_OF_CUSTOMERS, NUM_OF_ORDERS);
    }

    private void insertRows(String insertSql, String placeholders, List<Object[]> rows) {
        final int rowsPerStatement = 500;
        for (int from = 0; from < rows.size(); from += rowsPerStatement) {
            List<Object[]> chunk = rows.subList(from, Math.min(from + rowsPerStatement, rows.size()));
            StringBuilder sql = new StringBuilder(insertSql);
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                sql.append(i == 0 ? "" : ", ").append(placeholders);
                params.addAll(Arrays.asList(chunk.get(i)));
            }
            jdbcTemplate.update(sql.toString(), params.toArray());
        }
    }

    public static class Order {
        private int id;
        private int customerId;
        private String customerName;
        private String product;
        private int amount;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
