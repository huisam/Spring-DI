package core.di.factory.abnormal.circular;

import core.annotation.Inject;
import core.annotation.Service;

@Service
public class CircularService {

    private final CircularController circularController;

    @Inject
    public CircularService(CircularController circularController) {
        this.circularController = circularController;
    }
}
