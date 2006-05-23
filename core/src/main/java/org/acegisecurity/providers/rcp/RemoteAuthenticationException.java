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

package org.acegisecurity.providers.rcp;

import org.acegisecurity.AcegiSecurityException;


/**
 * Thrown if a <code>RemoteAuthenticationManager</code> cannot validate the presented authentication request.<P>This
 * is thrown rather than the normal <code>AuthenticationException</code> because <code>AuthenticationException</code>
 * contains additional properties which may cause issues for the remoting protocol.</p>
 *
 * @author Ben Alex
 * @version $Id$
 */
public class RemoteAuthenticationException extends AcegiSecurityException {
    //~ Constructors ===================================================================================================

/**
     * Constructs a <code>RemoteAuthenticationException</code> with the
     * specified message and no root cause.
     *
     * @param msg the detail message
     */
    public RemoteAuthenticationException(String msg) {
        super(msg);
    }
}
