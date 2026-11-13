package org.t246osslab.easybuggy4sb.vulnerabilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

@Controller
public class CommandInjectionController extends AbstractController {

    public static final String OSS_SRC = "oss-src";
    private static final String CLOC_DOWNLOAD_URL = "https://github.com/AlDanial/cloc/releases/download/v2.06/";
    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/commandijc")
    public ModelAndView process(@RequestParam(value = "url", required = false) String url,
                                ModelAndView mav, Locale locale) throws InterruptedException, IOException {
        setViewAndCommonObjects(mav, locale, "commandinjection");

        if (StringUtils.isNotBlank(url)) {
            try {
                Path dir = Paths.get(OSS_SRC);
                Files.createDirectories(dir);
                String repoDirName = getRepoDirName(url);
                FileUtils.deleteDirectory(new File(OSS_SRC + "/" + repoDirName));
                boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
                String shell = isWindows ? "cmd" : "bash";
                String shellOption = isWindows ? "/c" : "-c";
                String clocFile = isWindows ? "cloc-2.06.exe" : "cloc-2.06.pl";
                String clocCommand = (isWindows ? "" : "perl ") + clocFile + " --json oss-src/" + repoDirName;

                executeCommand(OSS_SRC, shell, shellOption, "git clone " + url);
                if (!new File(clocFile).exists()) {
                    executeCommand(null, shell, shellOption, "curl -O -L " + CLOC_DOWNLOAD_URL + clocFile);
                }
                Map<String, Object> clocResult = executeCloc(shell, shellOption, clocCommand);
                mav.addObject("cloc", clocResult);
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

    private void executeCommand(String directory, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        if (directory != null) pb.directory(new File(directory));
        Process process = pb.start();
        process.waitFor();
    }

    private Map<String, Object> executeCloc(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (InputStream is = process.getInputStream()) {
            String json = IOUtils.toString(is, StandardCharsets.UTF_8.name());
            process.waitFor();
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        }
    }

    public String getRepoDirName(String url) {
        if (StringUtils.isEmpty(url)) return null;
        int pos = url.lastIndexOf(".git");
        if (pos == -1) return null;
        String[] parts = url.substring(0, pos).split("/");
        if (parts.length < 3) return null;
        return parts[parts.length - 1];
    }
}
