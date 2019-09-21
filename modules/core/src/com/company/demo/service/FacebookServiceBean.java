package com.company.demo.service;

import com.company.demo.core.SocialRegistrationConfig;
import com.haulmont.cuba.core.global.Configuration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(FacebookService.NAME)
public class FacebookServiceBean extends AbstractOAuthServiceBean implements FacebookService {
    private static final String FACEBOOK_OAUTH_URL = "http://www.facebook.com/dialog/oauth";
    private static final String FACEBOOK_ME_URL = "https://graph.facebook.com/v2.8/me";
    private static final String FB_ACCESS_TOKEN_PATH = "https://graph.facebook.com/oauth/access_token";

    @Inject
    private Configuration configuration;

    @Override
    public String getLoginUrl(String appUrl, OAuth2ResponseType responseType) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String appId = config.getFacebookAppId();
        String scope = config.getFacebookAppScope();

        String redirectUrl = getEncodedUrl(appUrl);
        return FACEBOOK_OAUTH_URL + "?client_id=" + appId +
                "&response_type=" + responseType.getId() +
                "&redirect_uri=" + redirectUrl +
                "&scope=" + scope;
    }

    @Override
    protected HttpRequestBase getUserDataRequest(String accessToken) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String fields = config.getFacebookFields();
        String format = "json";
        String url = FACEBOOK_ME_URL + "?access_token=" + accessToken +
                "&fields=" + fields +
                "&format=" + format;
        return new HttpGet(url);
    }

    @Override
    protected HttpRequestBase getAccessTokenRequest(String code, String appUrl) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String redirectUrl = getEncodedUrl(appUrl);
        String appId = config.getFacebookAppId();
        String appSecret = config.getFacebookAppSecret();

        return new HttpGet(FB_ACCESS_TOKEN_PATH + "?client_id=" + appId
                + "&redirect_uri=" + redirectUrl +
                "&client_secret=" + appSecret + "&code=" + code);
    }

}