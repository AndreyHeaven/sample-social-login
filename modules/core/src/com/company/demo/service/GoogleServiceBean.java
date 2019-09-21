package com.company.demo.service;

import com.company.demo.core.SocialRegistrationConfig;
import com.haulmont.cuba.core.global.Configuration;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service(GoogleService.NAME)
public class GoogleServiceBean extends AbstractOAuthServiceBean implements GoogleService {
    private static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String USERINFO_API_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String ACCESS_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";

    @Inject
    private Configuration configuration;

    @Override
    public String getLoginUrl(String appUrl, GoogleService.OAuth2ResponseType responseType) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String appId = config.getGoogleAppId();
        String scope = getEncodedUrl(config.getGoogleAppScope());

        String redirectUrl = getEncodedUrl(appUrl);
        return OAUTH_URL + "?client_id=" + appId +
                "&response_type=" + responseType.getId() +
                "&redirect_uri=" + redirectUrl +
                "&scope=" + scope;
    }

    @Override
    protected HttpRequestBase getUserDataRequest(String accessToken) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String fields = config.getGoogleFields();
        String format = "json";
        String url = USERINFO_API_URL + "?access_token=" + accessToken +
                "&fields=" + fields +
                "&format=" + format;
        return new HttpGet(url);
    }

    @Override
    protected HttpRequestBase getAccessTokenRequest(String code, String appUrl) {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        String appId = config.getGoogleAppId();
        String appSecret = config.getGoogleAppSecret();
        HttpPost getRequest = new HttpPost(ACCESS_TOKEN_URL);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("client_id", appId));
        urlParameters.add(new BasicNameValuePair("client_secret", appSecret));
        urlParameters.add(new BasicNameValuePair("redirect_uri", appUrl));
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        try {
            getRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            //
        }
        return getRequest;
    }


}