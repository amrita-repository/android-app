/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import java.util.Map;

import in.co.rajkumaar.amritarepo.responses.BaseResponse;

public interface InitResponse extends BaseResponse {
    void onSuccess(String user_name, Map<String, Integer> Docu_type, Map<String, Integer> FIELD);
}
