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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_SECTION_NAME;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
// no listener configured
@Deploy({
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-core-types-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-life-cycle-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-content-template-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-adapters-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-notifications-contrib.xml" })
public class TestCreateSocialDocumentListener {

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    CreateSocialDocumentListener underTest;

    DocumentModel socialWorkspace;

    @Before
    public void setUp() throws Exception {
        underTest = new CreateSocialDocumentListener();
        socialWorkspace = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                SOCIAL_WORKSPACE_NAME, SocialConstants.SOCIAL_WORKSPACE_TYPE);
    }

    protected DocumentModel createDocumentModel(String path, String name,
            String type) throws ClientException {
        DocumentModel doc = session.createDocumentModel(path, name, type);
        doc = session.createDocument(doc);
        session.save();
        return doc;
    }

    @Test
    public void testListener() throws ClientException {

        DocumentModel privateNews = createDocumentModel(
                socialWorkspace.getPathAsString(), "private news",
                SocialConstants.NEWS_TYPE);

        underTest.publishSocialDocumentInPrivateSection(session, privateNews);

        DocumentRef privateNewsSection = new PathRef(
                socialWorkspace.getPathAsString() + "/" + SOCIAL_SECTION_NAME);

        DocumentModel publishedNews = session.getChild(privateNewsSection,
                privateNews.getName());
        assertNotNull(
                "A news called news 1 should be found as published in the private news section.",
                publishedNews);
        assertTrue("", publishedNews.isProxy());

        DocumentModel wrongPlacedNews = createDocumentModel("/",
                "wrong place of creation", SocialConstants.NEWS_TYPE);
        underTest.publishSocialDocumentInPrivateSection(session,
                wrongPlacedNews);

        String query = String.format(
                "SELECT * FROM Note WHERE ecm:path STARTSWITH '%s/' "
                        + "AND  ecm:isProxy =1 AND ecm:name ='%s'",
                socialWorkspace.getPathAsString(), wrongPlacedNews.getName());

        DocumentModelList unpublishedNews = session.query(query);

        assertEquals(
                "There should have no publication of \"wrong place of creation\"",
                0, unpublishedNews.size());

        DocumentModel socialDocumentFacetedNote = session.createDocumentModel(
                socialWorkspace.getPathAsString(), "Social Document Note",
                "Note");
        socialDocumentFacetedNote.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNote = session.createDocument(socialDocumentFacetedNote);
        session.save();

        underTest.publishSocialDocumentInPrivateSection(session,
                socialDocumentFacetedNote);

        DocumentModel publishedNote = session.getChild(privateNewsSection,
                socialDocumentFacetedNote.getName());

        assertNotNull(publishedNote);
    }

    @Test
    public void testEventHandle() throws Exception {
        DocumentModel privateNews = createDocumentModel(
                socialWorkspace.getPathAsString(), "private news",
                SocialConstants.NEWS_TYPE);

        EventContext context = new DocumentEventContext(session, null,
                privateNews);
        Event createDocumentEvent = new EventImpl("", context, 0);

        underTest.handleEvent(createDocumentEvent);

        DocumentRef privateNewsSectionRef = new PathRef(
                socialWorkspace.getPathAsString() + "/" + SOCIAL_SECTION_NAME);

        DocumentModel publishedNews = session.getChild(privateNewsSectionRef,
                privateNews.getName());

        assertNotNull(
                "A news called news 1 should be found as published in the private news section.",
                publishedNews);
        assertTrue("", publishedNews.isProxy());
    }

}
