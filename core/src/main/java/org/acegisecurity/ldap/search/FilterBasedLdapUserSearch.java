/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acegisecurity.ldap.search;

import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.ldap.LdapTemplate;
import org.acegisecurity.ldap.LdapUserSearch;

import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsImpl;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.util.Assert;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;


/**
 * LdapUserSearch implementation which uses an Ldap filter to locate the user.
 *
 * @author Robert Sanders
 * @author Luke Taylor
 * @version $Id$
 *
 * @see SearchControls
 */
public class FilterBasedLdapUserSearch implements LdapUserSearch {
    //~ Static fields/initializers =====================================================================================

    private static final Log logger = LogFactory.getLog(FilterBasedLdapUserSearch.class);

    //~ Instance fields ================================================================================================

    private InitialDirContextFactory initialDirContextFactory;
    private LdapUserDetailsMapper userDetailsMapper = new LdapUserDetailsMapper();

    /**
     * The LDAP SearchControls object used for the search. Shared between searches so shouldn't be modified
     * once the bean has been configured.
     */
    private SearchControls searchControls = new SearchControls();

    /** Context name to search in, relative to the root DN of the configured InitialDirContextFactory. */
    private String searchBase = "";

    /**
     * The filter expression used in the user search. This is an LDAP search filter (as defined in 'RFC 2254')
     * with optional arguments. See the documentation for the <tt>search</tt> methods in {@link
     * javax.naming.directory.DirContext DirContext} for more information.<p>In this case, the username is the
     * only parameter.</p>
     *  Possible examples are:
     *  <ul>
     *      <li>(uid={0}) - this would search for a username match on the uid attribute.</li>
     *  </ul>
     *  TODO: more examples.
     */
    private String searchFilter;

    //~ Constructors ===================================================================================================

    public FilterBasedLdapUserSearch(String searchBase, String searchFilter,
        InitialDirContextFactory initialDirContextFactory) {
        Assert.notNull(initialDirContextFactory, "initialDirContextFactory must not be null");
        Assert.notNull(searchFilter, "searchFilter must not be null.");
        Assert.notNull(searchBase, "searchBase must not be null (an empty string is acceptable).");

        this.searchFilter = searchFilter;
        this.initialDirContextFactory = initialDirContextFactory;
        this.searchBase = searchBase;

        if (searchBase.length() == 0) {
            logger.info("SearchBase not set. Searches will be performed from the root: "
                + initialDirContextFactory.getRootDn());
        }
    }

    //~ Methods ========================================================================================================

    /**
     * Return the LdapUserDetailsImpl containing the user's information
     *
     * @param username the username to search for.
     *
     * @return DOCUMENT ME!
     *
     * @throws UsernameNotFoundException if no matching entry is found.
     */
    public LdapUserDetails searchForUser(String username) {
        DirContext ctx = initialDirContextFactory.newInitialDirContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for user '" + username + "', in context " + ctx + ", with user search "
                + this.toString());
        }

        LdapTemplate template = new LdapTemplate(initialDirContextFactory);

        template.setSearchControls(searchControls);

        try {
            LdapUserDetailsImpl.Essence user = (LdapUserDetailsImpl.Essence) template.searchForSingleEntry(searchBase,
                    searchFilter, new String[] {username}, userDetailsMapper);
            user.setUsername(username);

            return user.createUserDetails();
        } catch (EmptyResultDataAccessException notFound) {
            throw new UsernameNotFoundException("User " + username + " not found in directory.");
        }
    }

    /**
     * Sets the corresponding property on the SearchControls instance used in the search.
     *
     * @param deref DOCUMENT ME!
     */
    public void setDerefLinkFlag(boolean deref) {
        searchControls.setDerefLinkFlag(deref);
    }

    /**
     * If true then searches the entire subtree as identified by context, if false (the default) then only
     * searches the level identified by the context.
     *
     * @param searchSubtree DOCUMENT ME!
     */
    public void setSearchSubtree(boolean searchSubtree) {
        searchControls.setSearchScope(searchSubtree ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE);
    }

    /**
     * The time (in milliseconds) which to wait before the search fails; the default is zero, meaning forever.
     *
     * @param searchTimeLimit DOCUMENT ME!
     */
    public void setSearchTimeLimit(int searchTimeLimit) {
        searchControls.setTimeLimit(searchTimeLimit);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[ searchFilter: '").append(searchFilter).append("', ");
        sb.append("searchBase: '").append(searchBase).append("'");
        sb.append(", scope: ")
          .append((searchControls.getSearchScope() == SearchControls.SUBTREE_SCOPE) ? "subtree" : "single-level, ");
        sb.append("searchTimeLimit: ").append(searchControls.getTimeLimit());
        sb.append("derefLinkFlag: ").append(searchControls.getDerefLinkFlag()).append(" ]");

        return sb.toString();
    }
}
