package org.t246osslab.easybuggy4sb.core.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the mini-CTF "hard mode" flag to every Thymeleaf view as
 * {@code hideHints}, so the hint info boxes can be suppressed when hard mode is
 * on (see {@code messages.html}).
 */
@ControllerAdvice
public class CtfModeAdvice {

    @Autowired
    private CtfModeService ctfModeService;

    @ModelAttribute(CtfModeService.HIDE_HINTS_ATTR)
    public boolean hideHints() {
        return ctfModeService.isHideHints();
    }
}
