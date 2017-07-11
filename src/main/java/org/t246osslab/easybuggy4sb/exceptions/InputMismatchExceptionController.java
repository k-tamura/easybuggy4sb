package org.t246osslab.easybuggy4sb.exceptions;

import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class InputMismatchExceptionController {

    @RequestMapping(value = "/ime")
    public void process() {
        try (Scanner scanner = new Scanner("a")) {
            scanner.nextInt();
        }
    }
}
