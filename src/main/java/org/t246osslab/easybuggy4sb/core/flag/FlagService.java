package org.t246osslab.easybuggy4sb.core.flag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Holds the "capture the flag" secrets for the mini-CTF mode and keeps track of
 * which flags have been captured.
 *
 * <p>Each flag is a secret string that can be obtained only by successfully
 * exploiting the corresponding vulnerability. To make the challenge less
 * trivial, the flag values are <strong>not stored in plain text</strong> in the
 * source code: they are kept obfuscated (XOR with a fixed key, then Base64) and
 * decoded only at runtime via {@link #value(String)}. This is deliberately a
 * weak, reversible scheme &mdash; it only stops the answers from being read by a
 * quick glance at (or {@code grep} of) the source tree.</p>
 *
 * <p>The decoded value is placed at each exploit's goal at runtime (see
 * {@code FlagInitializer} and the EL system property set below), so the plain
 * flag text does not appear in the repository.</p>
 *
 * <p>The captured set is persisted to a plain text file so that progress
 * survives restarts of EasyBuggy Boot. Progress is global (server-side), which
 * suits the single-user lab usage of EasyBuggy Boot.</p>
 */
@Service
public class FlagService {

    private static final Logger log = LoggerFactory.getLogger(FlagService.class);

    /** Key for the (deliberately simple) XOR obfuscation of flag values. */
    private static final byte[] KEY = "easybuggy".getBytes(StandardCharsets.UTF_8);

    /** System property that exposes the EL-injection flag (read via @System@getProperties()). */
    public static final String EL_FLAG_PROPERTY = "ctf.flag";

    /** id -&gt; obfuscated value (Base64 of XOR). Insertion order is the display order. */
    private final Map<String, String> encoded;

    /** Ids of flags captured so far (global, persisted). */
    private final Set<String> captured = Collections.synchronizedSet(new LinkedHashSet<String>());

    @Value("${flag.progress.file:data/flag-progress.txt}")
    private String progressFile;

    public FlagService() {
        Map<String, String> m = new LinkedHashMap<String, String>();
        // Obfuscated (not plain text). Decoded at runtime by decode().
        m.put("el_injection",          "Iy0yPkoQCzgQCwsWGhYcCAkmVwJEH1QUXldQ");             // 2.5
        m.put("file_disclosure",       "Iy0yPkoTDgscOgUaCgEZCBQMFwQsQANHBVAaUVda");         // 2.12
        m.put("pivoting",              "Iy0yPkoFDhEWEQgdHj1GA1AcXANDS0s=");                 // 2.13
        m.put("system_prompt_leakage", "Iy0yPkoGHhQNAAwsCRAaChcNOg0WGAkUAAImUgdDGFYXVV5Q"); // 2.16
        m.put("denial_of_wallet",      "Iy0yPkoRAgkQBA0sFgQqEAYVCQQHJlBFXl8cVVgVUA=="); // Denial of Wallet
        m.put("reverse_brute_force",   "Iy0yPkoHAhEcFxIWJgAHEhMcOgccCwEQOF4aVgREG1ZETg=="); // 3.11
        m.put("open_redirect",         "Iy0yPkoaFwIXOhMWHQsHAgQNOlhKH1IRBFMYTA==");         // 3.13
        m.put("idor",                  "Iy0yPkocAwgLOgJHGFsTVVdOTA==");                     // 3.18
        encoded = Collections.unmodifiableMap(m);
    }

    private static String decode(String b64) {
        byte[] x = Base64.getDecoder().decode(b64);
        byte[] out = new byte[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = (byte) (x[i] ^ KEY[i % KEY.length]);
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    /** Loads captured flags and publishes the EL flag as a system property. */
    @PostConstruct
    void init() {
        // Expose the EL-injection flag so it can be reached via @System@getProperties().
        String el = value("el_injection");
        if (el != null) {
            System.setProperty(EL_FLAG_PROPERTY, el);
        }
        // Load previously captured flags.
        Path p = Paths.get(progressFile);
        if (!Files.exists(p)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                String id = line.trim();
                if (encoded.containsKey(id)) {
                    captured.add(id);
                }
            }
            log.info("Loaded {} captured flag(s) from {}", captured.size(), p.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not read flag progress file ({}): {}", p.toAbsolutePath(), e.getMessage());
        }
    }

    private void save() {
        Path p = Paths.get(progressFile);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            List<String> lines;
            synchronized (captured) {
                lines = new ArrayList<String>(captured);
            }
            Files.write(p, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Could not write flag progress file ({}): {}", p.toAbsolutePath(), e.getMessage());
        }
    }

    // ---- flag definitions ----

    /** All flag ids in display order. */
    public Set<String> ids() {
        return encoded.keySet();
    }

    /** The decoded (plain text) secret value for a flag id, or {@code null} if unknown. */
    public String value(String id) {
        String b64 = encoded.get(id);
        return b64 == null ? null : decode(b64);
    }

    /** The total number of flags. */
    public int total() {
        return encoded.size();
    }

    /**
     * Returns the id of the flag whose secret equals the submitted text
     * (after trimming), or {@code null} if nothing matches.
     */
    public String idForSubmission(String submitted) {
        if (submitted == null) {
            return null;
        }
        String s = submitted.trim();
        for (String id : encoded.keySet()) {
            if (s.equals(value(id))) {
                return id;
            }
        }
        return null;
    }

    // ---- captured progress ----

    /**
     * Records the flag matching the submitted secret as captured and persists
     * the progress. Returns {@code true} if the submission matched a known flag.
     */
    public boolean capture(String submitted) {
        String id = idForSubmission(submitted);
        if (id == null) {
            return false;
        }
        if (captured.add(id)) {
            save();
        }
        return true;
    }

    public boolean isCaptured(String id) {
        return captured.contains(id);
    }

    public int capturedCount() {
        return captured.size();
    }

    /** True when every flag has been captured. */
    public boolean isComplete() {
        return total() > 0 && captured.size() >= total();
    }

    /** Clears all captured flags and persists the empty progress. */
    public void reset() {
        synchronized (captured) {
            captured.clear();
        }
        save();
    }
}
