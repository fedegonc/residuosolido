package com.residuosolido.app.enums;

import com.residuosolido.app.model.User;
import java.util.function.Predicate;

/**
 * Enum that defines the possible user roles in the system
 */
public enum Role {
    USER(user -> true),
    ORGANIZATION(user -> user.hasPhone() && user.getCity() != null && Boolean.TRUE.equals(user.getProfileCompleted()));

    private final Predicate<User> profileCompletionRule;

    Role(Predicate<User> profileCompletionRule) {
        this.profileCompletionRule = profileCompletionRule;
    }

    public boolean isProfileComplete(User user) {
        return profileCompletionRule.test(user);
    }
}
