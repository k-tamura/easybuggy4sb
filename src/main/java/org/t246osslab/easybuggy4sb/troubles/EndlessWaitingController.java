package org.t246osslab.easybuggy4sb.troubles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class EndlessWaitingController extends AbstractController {

    private static final int MAX_COUNT = 100000;

    @RequestMapping(value = "/endlesswaiting")
    public ModelAndView process(@RequestParam(value = "count", required = false) String strCount,
            HttpServletRequest req, ModelAndView mav, Locale locale) throws IOException {
        setViewAndCommonObjects(mav, locale, "endlesswaiting");
        int count = NumberUtils.toInt(strCount, 0);
        if (count > 0) {
            /* create a batch file in the temp directory */
            File batFile = createBatchFile(count,
                    req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString());

            if (batFile == null) {
                mav.addObject("errmsg", msg.getMessage("msg.cant.create.batch", null, locale));
            } else {
                try {
                    /* execte the batch */
                    ProcessBuilder pb = new ProcessBuilder(batFile.getAbsolutePath());
                    Process process = pb.start();
                    process.waitFor();
                    mav.addObject("msg",
                            msg.getMessage("msg.executed.batch", null, locale) + batFile.getAbsolutePath());
                    mav.addObject("result",
                            printInputStream(process.getInputStream()) + printInputStream(process.getErrorStream()));
                } catch (InterruptedException e) {
                    log.error("InterruptedException occurs: ", e);
                    mav.addObject("errmsg",
                            msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
                }
            }
        } else {
            mav.addObject("msg", msg.getMessage("msg.enter.positive.number", null, locale));
        }
        return mav;
    }

    private File createBatchFile(int count, String tmpdir) {

        String osName = System.getProperty("os.name").toLowerCase();
        String batFileName;
        String firstLine;
        if (osName.toLowerCase().startsWith("windows")) {
            batFileName = "test.bat";
            firstLine = "@echo off";
        } else {
            batFileName = "test.sh";
            firstLine = "#!/bin/sh";
        }

        File batFile;
        try {
            batFile = new File(tmpdir, batFileName);
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            return null;
        }
        try (FileWriter fileWriter = new FileWriter(batFile);
                BufferedWriter buffwriter = new BufferedWriter(fileWriter)) {
            if (!batFile.setExecutable(true)) {
                log.debug("batFile.setExecutable(true) returns false.");
            }
            buffwriter.write(firstLine);
            buffwriter.newLine();

            for (int i = 0; i < count && i < MAX_COUNT; i++) {
                if (i % 100 == 0) {
                    buffwriter.newLine();
                    buffwriter.write("echo ");
                }
                buffwriter.write(String.valueOf(i % 10));
            }
            buffwriter.close();
            fileWriter.close();
            if (!osName.toLowerCase().startsWith("windows")) {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("chmod 777 " + batFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        }
        return batFile;
    }

    private String printInputStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("<br>");
            }
        }
        return sb.toString();
    }
}
