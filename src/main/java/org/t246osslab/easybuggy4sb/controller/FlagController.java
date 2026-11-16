package org.t246osslab.easybuggy4sb.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.flag.CtfModeService;
import org.t246osslab.easybuggy4sb.core.flag.FlagService;

/**
 * Scoreboard and submission endpoint for the mini-CTF mode.
 *
 * <p>GET {@code /flags} shows every flag together with whether it has been
 * captured. POST {@code /flags} takes a submitted secret and, if it matches a
 * known flag, records that flag as captured. POST {@code /flags/reset} clears
 * all progress. The captured set is stored and persisted by {@link FlagService}
 * (globally, server-side), so progress survives restarts of EasyBuggy Boot.</p>
 */
@Controller
public class FlagController extends AbstractController {

    @Autowired
    protected FlagService flagService;

    @Autowired
    protected CtfModeService ctfModeService;

    @GetMapping("/flags")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        render(mav, locale);
        return mav;
    }

    @PostMapping("/flags")
    public ModelAndView submit(@RequestParam(value = "flag", required = false) String flag,
                               ModelAndView mav, Locale locale) {
        if (flagService.capture(flag)) {
            mav.addObject("msg", msg.getMessage("msg.flag.captured", null, locale));
        } else {
            mav.addObject("errmsg", msg.getMessage("msg.flag.invalid", null, locale));
        }
        render(mav, locale);
        return mav;
    }

    @PostMapping("/flags/reset")
    public ModelAndView reset(ModelAndView mav, Locale locale) {
        flagService.reset();
        mav.addObject("msg", msg.getMessage("msg.flag.reset", null, locale));
        render(mav, locale);
        return mav;
    }

    @PostMapping("/flags/mode")
    public ModelAndView toggleMode(ModelAndView mav, Locale locale) {
        ctfModeService.toggle();
        mav.addObject("msg", msg.getMessage(
                ctfModeService.isHideHints() ? "msg.flag.hardmode.on" : "msg.flag.hardmode.off", null, locale));
        render(mav, locale);
        return mav;
    }

    private void render(ModelAndView mav, Locale locale) {
        List<FlagView> rows = new ArrayList<FlagView>();
        for (String id : flagService.ids()) {
            boolean got = flagService.isCaptured(id);
            String title;
            try {
                title = msg.getMessage("flag.title." + id, null, locale);
            } catch (NoSuchMessageException e) {
                title = id;
            }
            rows.add(new FlagView(id, title, got, got ? flagService.value(id) : null));
        }
        setViewAndCommonObjects(mav, locale, "flags");
        mav.addObject("flags", rows);
        mav.addObject("capturedCount", flagService.capturedCount());
        mav.addObject("totalCount", flagService.total());
        mav.addObject("complete", flagService.isComplete());
    }

    /** View model for one row of the scoreboard. */
    public static class FlagView {
        private final String id;
        private final String title;
        private final boolean captured;
        private final String value;

        public FlagView(String id, String title, boolean captured, String value) {
            this.id = id;
            this.title = title;
            this.captured = captured;
            this.value = value;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public boolean isCaptured() { return captured; }
        public String getValue() { return value; }
    }
}
