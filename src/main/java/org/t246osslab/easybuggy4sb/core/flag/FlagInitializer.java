package org.t246osslab.easybuggy4sb.core.flag;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Places the mini-CTF flags at their exploit goals at runtime, using the decoded
 * values from {@link FlagService}. Keeping this out of {@code data.sql} and the
 * static web resources means the plain flag text does not sit in the source tree.
 *
 * <ul>
 *   <li>file disclosure (2.12): writes {@code /dl/flag.txt}</li>
 *   <li>pivoting (2.13): sets the {@code password} of the hidden user {@code ctf01}</li>
 * </ul>
 *
 * The EL-injection flag (2.5) is published as an environment variable by
 * {@link FlagService}, and the IDOR flag (3.18) is stored as a (hex) BLOB in
 * {@code data.sql}, so neither is handled here. Each step is best-effort: a
 * failure only makes that one flag uncapturable and is logged, not fatal.
 */
@Component
public class FlagInitializer {

    private static final Logger log = LoggerFactory.getLogger(FlagInitializer.class);

    @Autowired
    private FlagService flagService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ServletContext servletContext;

    @EventListener(ApplicationReadyEvent.class)
    public void placeFlags() {
        writeFileDisclosureFlag();
        setPivotingFlag();
    }

    private void setPivotingFlag() {
        String flag = flagService.value("pivoting");
        if (flag == null) {
            return;
        }
        try {
            int updated = jdbcTemplate.update("update users set password = ? where id = 'ctf01'", flag);
            if (updated == 0) {
                log.warn("Mini-CTF: user 'ctf01' not found; pivoting flag not set");
            } else {
                log.info("Mini-CTF: set pivoting flag on user 'ctf01'");
            }
        } catch (Exception e) {
            log.warn("Mini-CTF: could not set pivoting flag: {}", e.getMessage());
        }
    }

    private void writeFileDisclosureFlag() {
        String flag = flagService.value("file_disclosure");
        if (flag == null) {
            return;
        }
        try {
            String realPath = servletContext.getRealPath("/dl/flag.txt");
            File target = (realPath != null) ? new File(realPath)
                    : Paths.get("src", "main", "webapp", "dl", "flag.txt").toFile();
            if (target.getParentFile() != null) {
                target.getParentFile().mkdirs();
            }
            Files.write(target.toPath(), (flag + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            log.info("Mini-CTF: wrote file-disclosure flag to {}", target.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Mini-CTF: could not write file-disclosure flag: {}", e.getMessage());
        }
    }
}
