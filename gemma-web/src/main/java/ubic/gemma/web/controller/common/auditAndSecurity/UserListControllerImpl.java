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
package ubic.gemma.web.controller.common.auditAndSecurity;

import gemma.gsec.authentication.UserDetailsImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ubic.gemma.core.security.authentication.UserManager;
import ubic.gemma.model.common.auditAndSecurity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * For display and editing of users. Note: do not use parametrized collections as parameters for ajax methods in this
 * class! Type information is lost during proxy creation so DWR can't figure out what type of collection the method
 * should take. See bug 2756. Use arrays instead.
 *
 * @author pavlidis
 * @see UserFormMultiActionController
 * @see SignupController
 */
@Controller
public class UserListControllerImpl implements UserListController {

    private static final Log log = LogFactory.getLog( UserListControllerImpl.class.getName() );

    @Autowired
    private UserManager userManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Collection<UserValueObject> getUsers() {
        if ( log.isDebugEnabled() ) {
            log.debug( "entering 'getUsers' method..." );
        }

        Collection<UserValueObject> userValueObjects = new ArrayList<>();

        try {
            Collection<User> users = userManager.loadAll();
            for ( User u : users ) {
                UserValueObject uv = new UserValueObject( u );
                userValueObjects.add( uv );
            }
        } catch ( UncategorizedSQLException | LazyInitializationException e ) {
            log.error( e );
        }
        return userValueObjects;
    }

    @Override
    @RequestMapping(value = "/admin/activeUsers.html", method = RequestMethod.GET)
    public ModelAndView handleRequest( HttpServletRequest request, HttpServletResponse response ) {
        /*
         * FIXME: this lists all users, not the ones who are active.
         */
        Collection<User> users = null;
        try {
            users = userManager.loadAll();
        } catch ( UncategorizedSQLException | LazyInitializationException e ) {
            log.error( e );
        }

        return new ModelAndView( "/admin/activeUsers", "users", users );
    }

    @Override
    public void saveUser( UserValueObject user ) {

        String userName = user.getUserName();
        User u = userManager.findByUserName( userName );

        UserDetailsImpl userDetails;

        boolean newUser = false;
        if ( u == null ) {
            userDetails = new UserDetailsImpl( passwordEncoder.encodePassword( user.getPassword(), user.getUserName() ), user.getUserName(), false,
                    null, user.getEmail(), userManager.generateSignupToken( user.getUserName() ), new Date() );
        } else {
            u.setEmail( user.getEmail() );
            u.setEnabled( user.isEnabled() );
            userDetails = new UserDetailsImpl( u );
        }

        /*
         * When changing the roles (from user to say, admin), we must first create a new or update an existing user,
         * THEN change the acl_permission's acl_object_identity and mask. This must be done in two separate steps: first
         * add the acl permissions to the acl_permission and acl_object_identity tables (when creating a new user), then
         * change the permissions.
         */

        if ( newUser ) {
            userManager.createUser( userDetails );
        } else {
            userManager.updateUser( userDetails );
        }

    }

}
