/*
 * The Gemma project
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.gemma;

/**
 * Constant values used throughout the application.
 * <p>
 * Originally from Appfuse, to support code imported from Appfuse.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author pavlidis
 * @version $Id$
 */
public class Constants {
    // ~ Static fields/initializers =============================================

    /** The name of the ResourceBundle used in this application */
    public static final String BUNDLE_KEY = "messages";

    /**
     * The application scoped attribute for persistence engine and class that implements it
     */
    public static final String DAO_TYPE = "daoType";
    public static final String DAO_TYPE_HIBERNATE = "hibernate";

    /** The encryption algorithm key to be used for passwords */
    public static final String ENC_ALGORITHM = "algorithm";

    /** A flag to indicate if passwords should be encrypted */
    public static final String ENCRYPT_PASSWORD = "encryptPassword";

    /** File separator from System properties */
    public static final String FILE_SEP = System.getProperty( "file.separator" );

    /** User home from System properties */
    public static final String USER_HOME = System.getProperty( "user.home" ) + FILE_SEP;

    /**
     * The session scope attribute under which the User object for the currently logged in user is stored.
     */
    public static final String USER_KEY = "currentUserForm";

    /**
     * The request scope attribute under which an editable user form is stored
     */
    public static final String USER_EDIT_KEY = "userForm";

    /**
     * The request scope attribute that holds the user list
     */
    public static final String USER_LIST = "userList";

    /**
     * The request scope attribute for indicating a newly-registered user
     */
    public static final String REGISTERED = "registered";

    /**
     * The name of the Administrator role, as specified in web.xml
     */
    public static final String ADMIN_ROLE = "admin";

    /**
     * The name of the User role, as specified in web.xml
     */
    public static final String USER_ROLE = "user";

    /**
     * The name of the user's role list, a request-scoped attribute when adding/editing a user.
     */
    public static final String USER_ROLES = "userRoles";

    /**
     * The name of the available roles list, a request-scoped attribute when adding/editing a user.
     */
    public static final String AVAILABLE_ROLES = "availableRoles";

    /**
     * Name of cookie for "Remember Me" functionality.
     */
    public static final String LOGIN_COOKIE = "sessionId";

    /**
     * The name of the configuration hashmap stored in application scope.
     */
    public static final String CONFIG = "appConfig";
}
