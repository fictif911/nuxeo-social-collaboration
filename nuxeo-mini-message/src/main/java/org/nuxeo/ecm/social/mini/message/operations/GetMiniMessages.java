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

package org.nuxeo.ecm.social.mini.message.operations;

import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.ACTOR_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.FOR_ACTOR_STREAM_TYPE;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.LOCALE_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.RELATIONSHIP_KIND_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.STREAM_TYPE_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.MiniMessageHelper.toJSON;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
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
import org.nuxeo.ecm.social.mini.message.MiniMessage;

/**
 * Operation to get the mini messages for or from a given actor.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Operation(id = GetMiniMessages.ID, category = Constants.CAT_SERVICES, label = "Get mini messages", description = "Get mini messages for the current user.")
public class GetMiniMessages {

    public static final String ID = "Services.GetMiniMessages";

    public static final String PROVIDER_NAME = "gadget_mini_messages";

    public static final String ACTIVITIES_PROVIDER_NAME = "gadget_mini_message_activities";

    public static final String CIRCLE_KIND = "circle";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Param(name = "actor", required = false)
    protected String actor;

    @Param(name = "relationshipKind", required = false)
    protected String relationshipKind;

    @Param(name = "miniMessagesStreamType", required = false)
    protected String miniMessagesStreamType;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @Param(name = "asActivities", required = false)
    protected Boolean asActivities = false;

    @OperationMethod
    public Blob run() throws Exception {
        if (StringUtils.isBlank(relationshipKind)) {
            relationshipKind = CIRCLE_KIND;
        }

        if (StringUtils.isBlank(actor)) {
            actor = session.getPrincipal().getName();
        }
        if (StringUtils.isBlank(miniMessagesStreamType)) {
            miniMessagesStreamType = FOR_ACTOR_STREAM_TYPE;
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
        props.put(LOCALE_PROPERTY, locale);
        props.put(STREAM_TYPE_PROPERTY, miniMessagesStreamType);
        props.put(ACTOR_PROPERTY, actor);
        props.put(RELATIONSHIP_KIND_PROPERTY, relationshipKind);

        if (asActivities) {
            @SuppressWarnings("unchecked")
            PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                    ACTIVITIES_PROVIDER_NAME, null, targetLimit, 0L, props);
            pageProvider.setCurrentPageOffset(targetOffset);

            List<Map<String, Object>> miniMessages = new ArrayList<Map<String, Object>>();
            for (ActivityMessage activityMessage : pageProvider.getCurrentPage()) {
                Map<String, Object> o = activityMessage.toMap(session, locale);
                String actorUsername = getUsername(activityMessage.getActor());
                o.put("allowDeletion", session.getPrincipal().getName().equals(actorUsername));
                miniMessages.add(o);
            }

            Map<String, Object> m = new HashMap<String, Object>();
            m.put("offset", ((AbstractActivityPageProvider) pageProvider).getNextOffset());
            m.put("limit", pageProvider.getCurrentPageSize());
            m.put("miniMessages", miniMessages);

            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, m);
            return new StringBlob(writer.toString(), "application/json");
        } else {
            @SuppressWarnings("unchecked")
            PageProvider<MiniMessage> pageProvider = (PageProvider<MiniMessage>) pageProviderService.getPageProvider(
                    PROVIDER_NAME, null, targetLimit, 0L, props);
            pageProvider.setCurrentPageOffset(targetOffset);

            String json = toJSON(pageProvider, locale, session);
            return new StringBlob(json, "application/json");
        }
    }

}
