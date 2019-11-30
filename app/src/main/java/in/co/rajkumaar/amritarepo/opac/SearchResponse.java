/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import org.json.JSONArray;

import in.co.rajkumaar.amritarepo.responses.BaseResponse;

public interface SearchResponse extends BaseResponse {
    void onSuccess(JSONArray data, String action, String username);
}
