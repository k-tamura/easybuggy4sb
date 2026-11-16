package org.t246osslab.easybuggy4sb.core.flag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Holds the mini-CTF "hard mode" setting. In hard mode, the hint info boxes
 * (the {@code \faInfoCircle} notes that spell out how to attack each
 * vulnerability) are hidden, so the player has to find the attack themselves.
 *
 * <p>The setting is global (server-side) and persisted to a file so it survives
 * restarts, like the captured-flag progress. It is also mirrored to a
 * {@link ServletContext} attribute named {@code hideHints} so that the plain
 * JSP pages (LFI, SSRF) can honor it from JSP EL.</p>
 */
@Service
public class CtfModeService {

    private static final Logger log = LoggerFactory.getLogger(CtfModeService.class);

    /** ServletContext attribute / Thymeleaf model name read by the views. */
    public static final String HIDE_HINTS_ATTR = "hideHints";

    private volatile boolean hideHints = false;

    @Value("${flag.mode.file:data/ctf-mode.txt}")
    private String modeFile;

    @Autowired
    private ServletContext servletContext;

    @PostConstruct
    void load() {
        Path p = Paths.get(modeFile);
        if (Files.exists(p)) {
            try {
                String content = String.join("", Files.readAllLines(p, StandardCharsets.UTF_8)).trim();
                hideHints = "true".equalsIgnoreCase(content) || "1".equals(content);
            } catch (IOException e) {
                log.warn("Could not read CTF mode file ({}): {}", p.toAbsolutePath(), e.getMessage());
            }
        }
        publish();
    }

    public boolean isHideHints() {
        return hideHints;
    }

    /** Sets hard mode on/off and persists it. */
    public synchronized void setHideHints(boolean value) {
        this.hideHints = value;
        publish();
        save();
    }

    /** Flips hard mode and persists it. */
    public synchronized void toggle() {
        setHideHints(!hideHints);
    }

    private void publish() {
        if (servletContext != null) {
            servletContext.setAttribute(HIDE_HINTS_ATTR, hideHints);
        }
    }

    private void save() {
        Path p = Paths.get(modeFile);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.write(p, Collections.singletonList(Boolean.toString(hideHints)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Could not write CTF mode file ({}): {}", p.toAbsolutePath(), e.getMessage());
        }
    }
}
