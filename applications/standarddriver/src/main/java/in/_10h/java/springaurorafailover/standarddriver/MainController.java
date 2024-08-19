package in._10h.java.springaurorafailover.standarddriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private final MainService mainService;

    public MainController(
            final MainService mainService
    ) {
        this.mainService = mainService;
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-direct"})
    public String testRawDirect() {
        try {
            return this.mainService.testRawDirect().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-direct"})
    public String testUpdateRawDirect() {
        try {
            return this.mainService.testUpdateRawDirect().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-proxy"})
    public String testRawProxy() {
        try {
            return this.mainService.testRawProxy().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-proxy"})
    public String testUpdateRawProxy() {
        try {
            return this.mainService.testUpdateRawProxy().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    public String testWrapperSelfsplit() {
        try {
            return this.mainService.testWrapperSelfsplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    public String testUpdateWrapperSelfsplit() {
        try {
            return this.mainService.testUpdateWrapperSelfsplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    public String testWrapperDriversplit() {
        try {
            return this.mainService.testWrapperDriversplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    public String testUpdateWrapperDriversplit() {
        try {
            return this.mainService.testUpdateWrapperDriversplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }
}
