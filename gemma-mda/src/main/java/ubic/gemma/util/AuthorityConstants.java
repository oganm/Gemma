/*
 * The Gemma project
 * 
 * Copyright (c) 2006 Columbia University
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
package ubic.gemma.util;

import org.springframework.security.access.vote.AuthenticatedVoter;

/**
 * Defines constants used in GrantedAuthories. An authority is basically a marker of a level of access; in Gemma this
 * corresponds to the authority on a 'group' of users (UserGroup).
 * 
 * @author klc, paul
 * @version $Id$
 * @see org.springframework.security.core.GrantedAuthority
 */
public class AuthorityConstants {

    /**
     * The name of the administrator group authority. All administrators must be in this group.
     */
    public static final String ADMIN_GROUP = "GROUP_ADMIN";

    /**
     * The name of the default user group authority. All authenticated users should be in this group or in the
     * administrator group.
     */
    public static final String USER_GROUP = "GROUP_USER";

    public static final String IS_AUTHENTICATED_ANONYMOUSLY = AuthenticatedVoter.IS_AUTHENTICATED_ANONYMOUSLY;

    /**
     * Name of the 'group' for anonymous users. Note: we don't use this; you should use
     * AuthenticatedVoter.IS_AUTHENTICATED_ANONYMOUSLY instead.
     * 
     * @see org.springframework.security.access.vote.AuthenticatedVoter.IS_AUTHENTICATED_ANONYMOUSLY
     */
    public static final String ANONYMOUS_GROUP = "GROUP_ANONYMOUS";

    /**
     * Used when we are running at elevated permissions.
     */
    public static final String RUN_AS_ADMIN_AUTHORITY = "GROUP_RUN_AS_ADMIN";

    /**
     * 
     */
    public static final String AGENT_GROUP = "GROUP_AGENT";

}
