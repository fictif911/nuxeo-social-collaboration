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
 *     eugen,ronan
 */
package org.nuxeo.ecm.social.workspace;

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class SocialConstants {

    // Social Workspace
    public static final String SOCIAL_WORKSPACE_FACET = "SocialWorkspace";

    public static final String SOCIAL_WORKSPACE_TYPE = "SocialWorkspace";

    public static final String FIELD_SOCIAL_WORKSPACE_APPROVE_SUBSCRIPTION = "socialw:approveSubscription";

    public static final String FIELD_SOCIAL_WORKSPACE_IS_PUBLIC = "socialw:isPublic";

    public static final String PRIVATE_SECTION_RELATIVE_PATH = "private-section/";

    public static final String PUBLIC_SECTION_RELATIVE_PATH = "public-section/";

    public static final String SOCIAL_WORKSPACE_ACL_NAME = "socialWorkspaceAcl";

    public static final String VALIDATE_SOCIAL_WORKSPACE_TASK_NAME = "validateSocialWorkspace";


    // Social Document
    public static final String SOCIAL_DOCUMENT_FACET = "SocialDocument";

    public static final String FIELD_SOCIAL_DOCUMENT_IS_PUBLIC = "socialdoc:isPublic";


    public static final String ARTICLE_TYPE = "Article";

    public static final String NEWS_ITEM_TYPE = "NewsItem";


    public static final String CONTENT_PICTURE_PICTURE_FIELD = "contentpict:picture";

    public static final String FIELD_DC_TITLE = "dc:title";

    public static final String FIELD_NOTE_NOTE = "note:note";

    public static final String FIELD_DC_CREATED = "dc:created";

    public static final String FIELD_DC_AUTHOR = "dc:author";


    // Subscription Request
    public static final String REQUEST_DOC_TYPE = "Request";

    public static final String REQUEST_ROOT_NAME = "requests";

    public static final String REQUEST_SCHEMA = "request";

    public static final String REQUEST_TYPE_JOIN = "joinRequest";

    public static final String FIELD_REQUEST_USERNAME = "req:username";

    public static final String FIELD_REQUEST_TYPE = "req:type";

    public static final String FIELD_REQUEST_INFO = "req:info";

    public static final String FIELD_REQUEST_PROCESSED_DATE = "req:processedDate";

    public static final String FIELD_REQUEST_PROCESSED_COMMENT = "req:processedComment";

    public static final String NEWS_ROOT_RELATIVE_PATH = "news-root/";

    private SocialConstants() {
    }

}
