/**
 * Copyright © 2016, Okta, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scim.impl.api;


import com.scim.impl.service.ScimUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/scim/Users")
public class SingleUserController {

    private final ScimUserService scimUserService;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Map usersGet(@RequestParam Map<String, String> params) {
        return scimUserService.get(params);
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Map usersPost(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        return scimUserService.upsert(params, response);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Map singeUserGet(@PathVariable String id, HttpServletResponse response) {

        try {
            return scimUserService.getScimUser(id);
        } catch (Exception e) {
            response.setStatus(404);
            return scimError("User not found", Optional.of(404));
        }
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public @ResponseBody Map<String, Object> singleUserPut(@RequestBody Map<String, Object> payload,
                                           @PathVariable String id) {
        return scimUserService.update(payload, id);
    }


    @RequestMapping(value = "/{id}",method = RequestMethod.PATCH)
    public @ResponseBody Map<String, Object> singleUserPatch(@RequestBody Map<String, Object> payload,
                                                             @PathVariable String id) {
        return scimUserService.patch(payload, id, this);
    }

    public Map<String, Object> scimError(String message, Optional<Integer> statusCode) {

        Map<String, Object> returnValue = new HashMap<>();
        List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:Error");
        returnValue.put("schemas", schemas);
        returnValue.put("detail", message);

        returnValue.put("status", statusCode.orElse(500));
        return returnValue;
    }
}
