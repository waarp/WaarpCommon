/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.common.file.passthrough;

import goldengate.common.file.filesystembased.FilesystemBasedOptsMLSxImpl;

/**
 * Class that implements Opts command for MLSx operations. (-1) means not
 * supported, 0 supported but not active, 1 supported and active
 *
 * @author Frederic Bregier
 *
 */
public class PassthroughBasedOptsMLSxImpl extends FilesystemBasedOptsMLSxImpl {
    /**
     * Default empty constructor: no support at all of MLSx function
     */
    public PassthroughBasedOptsMLSxImpl() {
        super();
    }

    /**
     * (-1) means not supported, 0 supported but not active, 1 supported and
     * active
     *
     * @param optsSize
     * @param optsModify
     * @param optsType
     * @param optsPerm
     * @param optsCreate
     * @param optsUnique
     * @param optsLang
     * @param optsMediaType
     * @param optsCharset
     */
    public PassthroughBasedOptsMLSxImpl(byte optsSize, byte optsModify,
            byte optsType, byte optsPerm, byte optsCreate, byte optsUnique,
            byte optsLang, byte optsMediaType, byte optsCharset) {
        super();
    }
}
