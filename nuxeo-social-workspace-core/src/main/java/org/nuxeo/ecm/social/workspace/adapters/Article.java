/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     eugen
 */
package org.nuxeo.ecm.social.workspace.adapters;

import java.util.Calendar;

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public interface Article {

    String getTitle();

    void setTitle(String text);

    String getContent();

    void setContent(String text);

    String getAuthor();

    Calendar getCreated();

    String getFirstNCharacters(int n);
}
