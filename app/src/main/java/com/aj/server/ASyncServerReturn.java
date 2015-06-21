package com.aj.server;

import org.json.JSONObject;

/**
 * Created by Joost on 07/04/2015.
 */
public interface ASyncServerReturn {
    void onJSONReturn(JSONObject json);
}
