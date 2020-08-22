package core.di.factory.abnormal;

import core.annotation.Inject;
import core.annotation.web.Controller;

@Controller
public class CircularController {

    private final CircularService circularService;

    @Inject
    public CircularController(CircularService circularService) {
        this.circularService = circularService;
    }
}
