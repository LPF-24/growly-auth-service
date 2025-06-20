package com.example.auth_service.util;

public class SwaggerConstants {
    public static final String ID_DESC = "Unique identifier of the user";
    public static final String ID_EXAMPLE = "21";

    public static final String CODE_DESC = "A special code that, when entered, makes the user an admin. " +
            "It must be a six-digit number that is divisible by 4 without a remainder.";
    public static final String CODE_EXAMPLE = "400000";

    public static final String ACCESS_TOKEN_DESC = "A token that is used to identify and authorize a user when making " +
            "requests to an application within the JWT format";
    public static final String ACCESS_TOKEN_EXAMPLE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJVc2VyIGRldGFpbHMiLCJpZCI6OCwidXNlcm5hbWUiOiJtYXgiLCJyb2xlIjoiUk9MRV9BRE1JTiIsImlhdCI6MTc1MDQyMjgyNywiaXNzIjoiQURNSU4iLCJleHAiOjE3NTA0MjY0Mjd9.SnS70kFqBpEAyHSJfx_nPn48p6YKfwljz_2G-IAA7N4";

    public static final String USERNAME_DESC = "Unique username";
    public static final String USERNAME_EXAMPLE = "max";

    public static final String EMAIL_DESC = "Email address";
    public static final String EMAIL_EXAMPLE = "john@example.com";

    public static final String PASSWORD_DESC = "Password (min 8 characters, max 30 characters, " +
            "the first letter is capitalized, contains one digit, one special character and does not contain spaces)";
    public static final String PASSWORD_EXAMPLE = "Test234!";

    public static final String ROLE_DESC = "A representation of a user's authority in a system that determines access to resources and methods.";
    public static final String ROLE_EXAMPLE = "ROLE_ADMIN";

    public static final String LAST_LOGIN_DESC = "The last date and time the user logged in.";
    public static final String LAST_LOGIN_EXAMPLE = "2025-06-20T14:30:00";
}
