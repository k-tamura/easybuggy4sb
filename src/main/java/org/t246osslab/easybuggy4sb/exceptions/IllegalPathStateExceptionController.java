package org.t246osslab.easybuggy4sb.exceptions;

import java.awt.geom.GeneralPath;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IllegalPathStateExceptionController {

    @RequestMapping(value = "/ipse")
    public void process() {
        GeneralPath subPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 100);
        subPath.closePath();
    }
}
