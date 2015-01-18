/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.activity.stream.operations;

import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.ACTIVITY_LINK_BUILDER_NAME_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.FOR_ACTOR_STREAM_TYPE;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.LOCALE_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.STREAM_TYPE_PROPERTY;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter;
import org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider;

/**
 * Operation to get the activity stream for or from a given actor.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Operation(id = GetActivityStream.ID, category = Constants.CAT_SERVICES, label = "Get activity stream", description = "Get activity stream for the current user.")
public class GetActivityStream {

    public static final String ID = "Services.GetActivityStream";

    public static final String PROVIDER_NAME = "gadget_user_activity_stream";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Param(name = "actor", required = false)
    protected String actor;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "activityStreamType", required = false)
    protected String activityStreamType;

    @Param(name = "activityLinkBuilder", required = false)
    protected String activityLinkBuilder;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @SuppressWarnings("unchecked")
    @OperationMethod
    public Blob run() throws Exception {
        if (StringUtils.isBlank(actor)) {
            actor = session.getPrincipal().getName();
        }
        if (StringUtils.isBlank(activityStreamType)) {
            activityStreamType = FOR_ACTOR_STREAM_TYPE;
        }

        Long targetOffset = 0L;
        if (offset != null) {
            targetOffset = offset.longValue();
        }
        Long targetLimit = null;
        if (limit != null) {
            targetLimit = limit.longValue();
        }

        Locale locale = language != null && !language.isEmpty() ? new Locale(language) : Locale.ENGLISH;

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(UserActivityStreamFilter.ACTOR_PARAMETER, actor);
        props.put(STREAM_TYPE_PROPERTY, activityStreamType);
        props.put(ACTIVITY_LINK_BUILDER_NAME_PROPERTY, activityLinkBuilder);
        props.put(LOCALE_PROPERTY, locale);
        props.put(CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetLimit, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        List<Map<String, Object>> activities = new ArrayList<Map<String, Object>>();
        for (ActivityMessage activityMessage : pageProvider.getCurrentPage()) {
            activities.add(activityMessage.toMap(session, locale, activityLinkBuilder));
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("offset", ((UserActivityStreamPageProvider) pageProvider).getNextOffset());
        m.put("limit", pageProvider.getPageSize());
        m.put("activities", activities);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new StringBlob(writer.toString(), "application/json");
    }

}
