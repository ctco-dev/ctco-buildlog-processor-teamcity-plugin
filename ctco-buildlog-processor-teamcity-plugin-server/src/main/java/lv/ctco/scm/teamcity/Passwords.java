package lv.ctco.scm.teamcity;

/*
 * @(#)Passwords.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauči, Ķekava district, LV-1076, Latvia. All rights reserved.
 */

import jetbrains.buildServer.serverSide.SBuild;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Passwords {

    @NotNull
    public Map<String, String> getPasswordsToReplace(@NotNull final SBuild build) {
        final String key = "hide.regex";
        final String value = build.getParametersProvider().get(key);
        final String httpsKey = "https_key";
        final String httpsRegex = "http.*://.*:.*@.*";

        Map<String, String> map = new HashMap<String, String>();
        map.put(httpsKey, httpsRegex);
        if (value != null) {
            map.put(key, value);
        }
        return map;
    }
}