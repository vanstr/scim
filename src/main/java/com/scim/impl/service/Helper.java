package com.scim.impl.service;

import java.util.Map;

public class Helper {


    public static int getCount(Map<String, String> params) {
        return (params.get("count") != null) ? Integer.parseInt(params.get("count")) : 100;
    }

    public static int getStartIndex(Map<String, String> params) {
        int startIndex = (params.get("startIndex") != null) ? Integer.parseInt(params.get("startIndex")) : 1;

        if (startIndex < 1) {
            startIndex = 1;
        }
        startIndex -= 1;
        return startIndex;
    }
}
