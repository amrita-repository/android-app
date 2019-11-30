/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import org.json.JSONObject;

import in.co.rajkumaar.amritarepo.responses.BaseResponse;

public interface BookDetailResponse extends BaseResponse {
    void onSuccess(JSONObject jsonObject);
}
