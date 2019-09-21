package com.company.demo.service;

import com.company.demo.core.SocialRegistrationConfig;
import com.haulmont.cuba.core.global.Configuration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(VkService.NAME)
public class VkServiceBean extends AbstractOAuthServiceBean implements VkService {
    private static final String OAUTH_URL = "https://oauth.vk.com/authorize";
    private static final String USERINFO_API_URL = "https://api.vk.com/method/account.getProfileInfo";
    private static final String ACCESS_TOKEN_URL = "https://oauth.vk.com/access_token";
    @Inject
    private Configuration configuration;

    @Override
    protected HttpRequestBase getUserDataRequest(String accessToken) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String fields = config.getVkFields();
        String format = "json";
        String url = USERINFO_API_URL + "?access_token=" + accessToken +
                "&v=5.101";
        return new HttpGet(url);
    }


    @Override
    public String getLoginUrl(String appUrl, OAuth2ResponseType responseType) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String appId = config.getVkAppId();
        String scope = config.getVkAppScope();

        String redirectUrl = getEncodedUrl(appUrl);
        return OAUTH_URL + "?client_id=" + appId +
                "&response_type=" + responseType.getId() +
                "&redirect_uri=" + redirectUrl +
                "&scope=" + scope + "&display=page";

    }

    @Override
    protected HttpRequestBase getAccessTokenRequest(String code, String appUrl) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String redirectUrl = getEncodedUrl(appUrl);
        String appId = config.getVkAppId();
        String appSecret = config.getVkAppSecret();
        return new HttpGet(ACCESS_TOKEN_URL + "?client_id=" + appId
                + "&redirect_uri=" + redirectUrl +
                "&client_secret=" + appSecret + "&code=" + code);
    }

}