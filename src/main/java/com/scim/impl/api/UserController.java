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


import com.scim.impl.service.ScimException;
import com.scim.impl.service.ScimUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/scim/v2/Users")
public class UserController {

    public final ScimUserService scimUserService;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Map usersGet(@RequestParam Map<String, String> params) {
        return scimUserService.get(params);
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Map usersPost(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        return scimUserService.upsert(params, response);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> singeUserGet(@PathVariable String id, HttpServletResponse response) {
        try {
            return scimUserService.getScimUser(id);
        } catch (Exception e) {
            response.setStatus(404);
            return scimUserService.scimError("User not found", Optional.of(404));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Map<String, Object> deleteUser(@PathVariable String id, HttpServletResponse response) {
        try {
            return scimUserService.deleteById(id);
        } catch (Exception e) {
            response.setStatus(404);
            return scimUserService.scimError("User not found", Optional.of(404));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public @ResponseBody Map<String, Object> singleUserPut(@RequestBody Map<String, Object> payload,
                                                           @PathVariable String id) {
        return scimUserService.update(payload, id);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public @ResponseBody Map<String, Object> singleUserPatch(@RequestBody Map<String, Object> payload,
                                                             @PathVariable String id, HttpServletResponse response) {
        try {
            return scimUserService.patch(payload, id, response);
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            return scimUserService.scimError(se.getMessage(), Optional.of(se.getErrorCode()));
        } catch (Exception e) {
            response.setStatus(500);
            return scimUserService.scimError("Error" + e.getMessage(), Optional.of(500));
        }
    }

}
