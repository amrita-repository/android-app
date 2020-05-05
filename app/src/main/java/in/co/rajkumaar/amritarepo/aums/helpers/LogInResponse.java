/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.helpers;

public abstract class LogInResponse {

    public abstract void onSuccess();

    public abstract void onFailure();

    public abstract void onException(Exception e);
}
