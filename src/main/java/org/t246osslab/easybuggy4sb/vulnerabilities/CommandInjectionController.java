package org.t246osslab.easybuggy4sb.vulnerabilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ognl.DefaultMemberAccess;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Controller
public class CommandInjectionController extends AbstractController {

    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/commandijc")
    public ModelAndView process(@RequestParam(value = "url", required = false) String url,
                                ModelAndView mav, Locale locale) throws InterruptedException, IOException {
        setViewAndCommonObjects(mav, locale, "commandinjection");
        OgnlContext ctx = new OgnlContext();
        ctx.setMemberAccess(new DefaultMemberAccess(false));
        String repoDirName = getRepoDirName(url);
        if (!StringUtils.isBlank(url)) {
            try {
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.startsWith("windows")) {
                    if (!new File("oss-src/" + repoDirName).exists()) {
                        executeCommand("cmd", "/c", "git clone " + url + " oss-src/" + repoDirName);
                    }
                    if (!new File("cloc-2.06.exe").exists()) {
                        executeCommand("cmd", "/c", "curl -O -L https://github.com/AlDanial/cloc/releases/download/v2.06/cloc-2.06.exe");
                    }
                    Map<String, Object> clocResult = executeCloc("cmd", "/c", "cloc-2.06.exe --json oss-src/" + repoDirName);
                    mav.addObject("cloc", clocResult);
                } else {
                    if (!new File("oss-src/" + repoDirName).exists()) {
                        executeCommand("bash", "-c", "git clone " + url + " oss-src/" + repoDirName);
                    }
                    if (!new File("cloc-2.06.pl").exists()) {
                        executeCommand("bash", "-c", "curl -O -L https://github.com/AlDanial/cloc/releases/download/v2.06/cloc-2.06.pl");
                    }
                    Map<String, Object> clocResult = executeCloc("bash", "-c", "perl cloc-2.06.pl --json oss-src/" + repoDirName);
                    mav.addObject("cloc", clocResult);
                }
            } catch (Exception e) {
                log.error("Exception occurs: ", e);
                mav.addObject("errmsg", msg.getMessage("msg.cant.analyze.code", null, null, locale));
                mav.addObject("detailmsg", e.getMessage());
            }
        }
        if (url != null) {
            mav.addObject("expression", url);
        }
        return mav;
    }

    private void executeCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
    }

    private Map<String, Object> executeCloc(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (InputStream is = process.getInputStream()) {
            String json = convertStreamToString(is);
            process.waitFor();
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        }
    }

    private String convertStreamToString(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[2048];
        int len;
        while ((len = is.read(buf)) != -1) {
            sb.append(new String(buf, 0, len, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    public String getRepoDirName(String url) {
        if (url == null || url.isEmpty()) return "";
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.endsWith(".git") ? last.substring(0, last.length() - 4) : last;
    }
}
