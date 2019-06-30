/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import java.util.Map;

import in.co.rajkumaar.amritarepo.responses.BaseResponse;

public interface SearchResponse extends BaseResponse {
    void onSuccess(Map<String, Integer> results);
}
