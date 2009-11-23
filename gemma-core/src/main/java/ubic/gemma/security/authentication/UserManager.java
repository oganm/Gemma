/*
 * The Gemma_sec1 project
 * 
 * Copyright (c) 2009 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.gemma.security.authentication;

import java.util.Collection;

import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;

import ubic.gemma.model.common.auditAndSecurity.User;

/**
 * @author paul
 * @version $Id$
 */
public interface UserManager extends UserDetailsManager, GroupManager {

    /**
     * @return the current user, or null if anonymous
     */
    public User getCurrentUser();

    public String getCurrentUsername();

    /**
     * @param username
     * @return names of groups the user is in.
     */
    public Collection<String> findGroupsForUser( String username );

    /**
     * Sign in the user identified
     * 
     * @param userName
     * @param password
     */
    public void reauthenticate( String userName, String password );

    /**
     * Generate a token that can be used to check if the user's email is valid.
     * 
     * @param username
     * @return
     */
    public String generateSignupToken( String username );

    /**
     * @return list of all available usernames.
     */
    public Collection<String> findAllUsers();

    /**
     * Validate the token.
     * 
     * @param username
     * @param key
     * @return true if okay, false otherwise
     */
    public boolean validateSignupToken( String username, String key );

}