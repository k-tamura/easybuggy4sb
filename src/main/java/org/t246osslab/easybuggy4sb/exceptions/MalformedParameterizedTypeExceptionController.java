package org.t246osslab.easybuggy4sb.exceptions;

import java.lang.reflect.Type;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

@Controller
public class MalformedParameterizedTypeExceptionController {

	@RequestMapping(value = "/mpte")
	public void process() {
        ParameterizedTypeImpl.make(List.class, new Type[]{}, null);
	}
}