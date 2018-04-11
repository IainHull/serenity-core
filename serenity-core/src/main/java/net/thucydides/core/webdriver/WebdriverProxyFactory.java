package net.thucydides.core.webdriver;

import net.serenitybdd.core.collect.*;
import net.serenitybdd.core.environment.*;
import net.thucydides.core.steps.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;

import static java.util.Collections.*;

/**
 * Provides a proxy for a WebDriver instance.
 * The proxy lets you delay opening the browser until you really know you are going to use it.
 */
public class WebdriverProxyFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private static ThreadLocal<WebdriverProxyFactory> factory = new ThreadLocal<WebdriverProxyFactory>();

    private static List<ThucydidesWebDriverEventListener> eventListeners
                                              = synchronizedList(new ArrayList<ThucydidesWebDriverEventListener>());

    private WebDriverFactory webDriverFactory;
    private WebDriverFacade mockDriver;
    private final DriverConfiguration configuration;

    private WebdriverProxyFactory() {
        webDriverFactory = new WebDriverFactory();
        this.configuration = WebDriverConfiguredEnvironment.getDriverConfiguration();
    }

    public static WebdriverProxyFactory getFactory() {
        if (factory.get() == null) {
            factory.set(new WebdriverProxyFactory());
        }
        return factory.get();
    }

    public static List<ThucydidesWebDriverEventListener> getEventListeners() {
        return NewList.copyOf(eventListeners);
    }
    public WebDriverFacade proxyFor(final Class<? extends WebDriver> driverClass) {
       return proxyFor(driverClass,
                       new WebDriverFactory(),
                       ConfiguredEnvironment.getConfiguration());
    }

    public WebDriverFacade proxyFor(final Class<? extends WebDriver> driverClass,
                                    final WebDriverFactory webDriverFactory,
                                    Configuration configuration) {
        return proxyFor(driverClass,webDriverFactory,configuration,"");
    }

    public WebDriverFacade proxyFor(final Class<? extends WebDriver> driverClass,
                                    final WebDriverFactory webDriverFactory,
                                    Configuration configuration,
                                    String options) {
        if (mockDriver != null) {
            return mockDriver;
        } else {
            return new WebDriverFacade(driverClass, webDriverFactory, configuration.getEnvironmentVariables()).withOptions(options);
        }
    }

    public WebDriverFacade proxyFor(WebDriver driver) {
        return new WebDriverFacade(driver, webDriverFactory, configuration.getEnvironmentVariables());
    }


    public void registerListener(final ThucydidesWebDriverEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void notifyListenersOfWebdriverCreationIn(final WebDriverFacade webDriverFacade) {
        for(ThucydidesWebDriverEventListener listener : getEventListeners()) {
            listener.driverCreatedIn(webDriverFacade);
        }
    }

    public WebDriver proxyDriver() {
        Class<? extends WebDriver> driverClass = webDriverFactory.getClassFor(configuration.getDriverType());
        return proxyFor(driverClass,
                        webDriverFactory,
                        ConfiguredEnvironment.getConfiguration());
    }

    public static void resetDriver(final WebDriver driver) {
        if (driver instanceof WebDriverFacade) {
            ((WebDriverFacade) driver).reset();
        }
    }

    public void useMockDriver(final WebDriverFacade mockDriver) {
        this.mockDriver = mockDriver;
    }

    public void clearMockDriver() {
        this.mockDriver = null;
    }

    public static void clearBrowserSession(WebDriver driver) {

        if (StepEventBus.getEventBus().isDryRun()) { return; }

        if (((WebDriverFacade) driver).isInstantiated()) {
            driver.manage().deleteAllCookies();
            try {
                ((JavascriptExecutor) driver).executeScript(String.format("window.localStorage.clear();"));
            } catch (WebDriverException driverDoesntSupportJavascriptTooBad) {}
        }
    }
}
