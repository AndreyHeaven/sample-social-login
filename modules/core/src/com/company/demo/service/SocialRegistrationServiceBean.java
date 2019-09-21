package com.company.demo.service;

import com.company.demo.core.SocialRegistrationConfig;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service(SocialRegistrationService.NAME)
public class SocialRegistrationServiceBean implements SocialRegistrationService {
    @Inject
    private Metadata metadata;

    @Inject
    private Persistence persistence;

    @Inject
    private Configuration configuration;

    @Override
    @Transactional
    public User findOrRegisterUser(String socialId, String email, String name) {
        EntityManager em = persistence.getEntityManager();

        // Find existing user
        TypedQuery<User> query = em.createQuery("select u from sec$User u where u.email = :email",
                User.class);
        query.setParameter("email", email);
        query.setViewName(View.LOCAL);

        User existingUser = query.getFirstResult();
        if (existingUser != null) {
            return existingUser;
        }

        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        Group defaultGroup = em.find(Group.class, config.getDefaultGroupId(), View.MINIMAL);

        // Register new user
        User user = metadata.create(User.class);
//        user.setFacebookId(facebookId);
        user.setEmail(email);
        user.setName(name);
        user.setGroup(defaultGroup);
        user.setActive(true);
        user.setLogin(email);

        em.persist(user);

        return user;
    }
}