/*
 *  The MIT License
 *
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.backlogtool.permission;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class evaluates permission checks and delegates to
 * one of the permission classes.
 */
public class BacklogPermissionEvaluator implements PermissionEvaluator {

    private Map<String, Permission> permissionMap = new HashMap<String, Permission>();

    public BacklogPermissionEvaluator(Map<String, Permission> permissionMap) {
        this.permissionMap = permissionMap;
    }

    @Override
    @Transactional
    /**
     * Checks if authenticated user has the required permission for an object.
     */
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permissionObject) {
        boolean hasPermission = false;
        if (isValid(authentication, targetDomainObject, permissionObject)) {
            String permissionString = (String) permissionObject;
            verifyPermissionIsDefined(permissionString);
            Permission permission = permissionMap.get(permissionString);
            hasPermission = permission.isAllowed(authentication, targetDomainObject);
        }
        return hasPermission;
    }

    /**
     * Checks if the arguments are valid
     * @param authentication the authenticated user
     * @param targetDomainObject accessed object
     * @param permission required permission
     * @return
     */
    private boolean isValid (Authentication authentication, Object targetDomainObject, Object permission) {
        return authentication != null && permission instanceof String;
    }

    /**
     * Checks if the specified permission is defined.
     * @param permissionKey permission to check
     */
    private void verifyPermissionIsDefined(String permissionKey) {
        if (!permissionMap.containsKey(permissionKey)) {
            try {
                throw new Exception("No permission with key " + permissionKey + " is defined in " + this.getClass().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Not supported.
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        try {
            throw new Exception("Id and Class permissions are not supported by " + this.getClass().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
