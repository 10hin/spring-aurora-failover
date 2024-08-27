package in._10h.java.springaurorafailover.standarddriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public String testCreateRawDirect() {
        try {
            return this.mainService.testInsertRawDirect().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PutMapping(path = "/test/{id}", headers = {"data-source=raw-direct"})
    public String testUpdateRawDirect(
            @PathVariable("id")
            final Integer id
    ) {
        try {
            return this.mainService.testUpdateRawDirect(id).toString();
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
    public String testCreateRawProxy() {
        try {
            return this.mainService.testInsertRawProxy().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PutMapping(path = "/test/{id}", headers = {"data-source=raw-proxy"})
    public String testUpdateRawProxy(
            @PathVariable("id")
            final Integer id
    ) {
        try {
            return this.mainService.testUpdateRawProxy(id).toString();
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
    public String testCreateWrapperSelfsplit() {
        try {
            return this.mainService.testInsertWrapperSelfsplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PutMapping(path = "/test/{id}", headers = {"data-source=wrapper-selfsplit"})
    public String testUpdateWrapperSelfsplit(
            @PathVariable("id")
            final Integer id
    ) {
        try {
            return this.mainService.testUpdateWrapperSelfsplit(id).toString();
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
    public String testCreateWrapperDriversplit() {
        try {
            return this.mainService.testInsertWrapperDriversplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PutMapping(path = "/test/{id}", headers = {"data-source=wrapper-driversplit"})
    public String testUpdateWrapperDriversplit(
            @PathVariable("id")
            final Integer id
    ) {
        try {
            return this.mainService.testUpdateWrapperDriversplit(id).toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }
}
