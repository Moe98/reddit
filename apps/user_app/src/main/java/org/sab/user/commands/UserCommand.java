package org.sab.user.commands;


import org.sab.service.validation.CommandWithVerification;



public abstract class UserCommand extends CommandWithVerification {
    protected static final String USERNAME = "username";
    protected static final String EMAIL = "email";
    protected static final String PASSWORD = "password";
    protected static final String BIRTHDATE = "birthdate";
    protected static final String PHOTO_URL = "photoUrl";
    protected static final String New_PASSWORD = "newPassword";




}
