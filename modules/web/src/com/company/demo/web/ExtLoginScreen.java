package com.company.demo.web;

import com.company.demo.service.*;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.executors.UIAccessor;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.security.ExternalUserCredentials;
import com.vaadin.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

@UiController("login")
@UiDescriptor("ext-login-screen.xml")
public class ExtLoginScreen extends LoginScreen {
    private final Logger log = LoggerFactory.getLogger(ExtLoginScreen.class);
    @Inject
    private BackgroundWorker backgroundWorker;
    @Inject
    private SocialRegistrationService socialRegistrationService;
    @Inject
    private FacebookService facebookService;
    @Inject
    private GoogleService googleService;

    @Inject
    private VkService vkService;

    @Inject
    private GlobalConfig globalConfig;
    private URI redirectUri;
    private UIAccessor uiAccessor;



    @Override
    protected void onInit(InitEvent event) {
        super.onInit(event);
        this.uiAccessor = backgroundWorker.getUIAccessor();
    }
    @Subscribe("facebookBtn")
    private void onFacebookBtnClick(Button.ClickEvent event) {
        handleCallBackRequest(facebookService);
    }

    @Subscribe("googleBtn")
    private void onGoogleBtnClick(Button.ClickEvent event) {
        handleCallBackRequest(googleService);
    }

    @Subscribe("vkBtn")
    private void onVkBtnClick(Button.ClickEvent event) {
        handleCallBackRequest(vkService);
    }

    public void handleCallBackRequest(SocialLoginService service) {
        VaadinSession.getCurrent()
                .addRequestHandler(new RequestHandler() {
                    @Override
                    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
                        if (request.getParameter("code") != null) {
                            uiAccessor.accessSynchronously(new Auth(this,
                                    request.getParameter("code"), session, service));
                            ((VaadinServletResponse) response).getHttpServletResponse().
                                    sendRedirect(ControllerUtils.getLocationWithoutParams(redirectUri));
                            return true;
                        }
                        return false;
                    }
                });

        this.redirectUri = Page.getCurrent().getLocation();

        String loginUrl = service.getLoginUrl(globalConfig.getWebAppUrl(),
                SocialLoginService.OAuth2ResponseType.CODE);
        Page.getCurrent()
                .setLocation(loginUrl);

    }





    class Auth implements Runnable {
        private VaadinSession session;
        private SocialLoginService socialLoginService;
        private RequestHandler requestHandler;
        private String code;

        Auth(RequestHandler requestHandler, String code, VaadinSession session, SocialLoginService socialLoginService) {
            this.requestHandler = requestHandler;
            this.code = code;
            this.session = session;
            this.socialLoginService = socialLoginService;
        }

        @Override
        public void run() {
            try {
                SocialLoginService.UserData userData = socialLoginService.getUserData(globalConfig.getWebAppUrl(), code);
                User user = socialRegistrationService.findOrRegisterUser(
                        userData.getId(), userData.getEmail(), userData.getName());
                Connection connection = app.getConnection();
                Locale defaultLocale = messages.getTools().getDefaultLocale();
                connection.login(new ExternalUserCredentials(user.getLogin(), defaultLocale));
            } catch (Exception e) {
                log.error("Unable to login using Social net", e);
            } finally {
                session.removeRequestHandler(requestHandler);
            }
        }
    }
}