package com.company.demo.service;

import com.haulmont.cuba.security.entity.User;

public interface SocialRegistrationService {
    String NAME = "sociallogindemo_SocialRegistrationService";

    User findOrRegisterUser(String socialId, String email, String name);

}