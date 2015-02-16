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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ITEM_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 */
// no listener configured
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.features" })
@LocalDeploy("org.nuxeo.ecm.automation.core:override-social-workspace-operation-chains-contrib.xml")
public class TestVisibilityManagement extends AbstractSocialWorkspaceTest {

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

    private static final Log log = LogFactory.getLog(TestVisibilityManagement.class);

    @Inject
    protected EventService eventService;

    protected DocumentModel socialWorkspaceDoc;

    protected SocialWorkspace socialWorkspace;

    protected NuxeoPrincipal notMember;

    protected NuxeoPrincipal member;

    protected NuxeoPrincipal administrator;

    @Before
    public void setUp() throws Exception {
        socialWorkspace = createSocialWorkspace(SOCIAL_WORKSPACE_NAME, true);
        socialWorkspaceDoc = socialWorkspace.getDocument();

        String membersGroup = socialWorkspace.getMembersGroupName();
        String administratorsGroup = socialWorkspace.getAdministratorsGroupName();

        notMember = new NuxeoPrincipalImpl("notMember");
        member = createUserWithGroup("member", membersGroup);
        assertTrue(member.getAllGroups().contains(membersGroup));
        administrator = createUserWithGroup("administratorOfSW", administratorsGroup);
    }

    @Test
    public void testShouldNotBeSocialDocument() throws Exception {
        DocumentModel outOfSocialWorkspace = createDocument("/", "wrong place of creation", ARTICLE_TYPE);
        assertNull(toSocialDocument(outOfSocialWorkspace));
    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentPrivate() throws Exception {
        DocumentModel privateNews = createSocialDocument(socialWorkspaceDoc.getPathAsString(), "private news",
                NEWS_ITEM_TYPE, false);
        SocialDocument socialDocument = toSocialDocument(privateNews);

        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());
    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentWithFacetAdded() throws Exception {
        DocumentModel socialDocumentFacetedNotePrivate = session.createDocumentModel(
                socialWorkspaceDoc.getPathAsString(), "Social Document Note1", "Note");
        socialDocumentFacetedNotePrivate.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNotePrivate = session.createDocument(socialDocumentFacetedNotePrivate);
        session.save();
        eventService.waitForAsyncCompletion();
        session.save();
        socialDocumentFacetedNotePrivate = session.getDocument(socialDocumentFacetedNotePrivate.getRef());

        SocialDocument socialDocument = toSocialDocument(socialDocumentFacetedNotePrivate);
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        DocumentModel socialDocumentFacetedNotePublic = session.createDocumentModel(
                socialWorkspaceDoc.getPathAsString(), "Social Document Note2", "Note");
        socialDocumentFacetedNotePublic.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNotePublic.setPropertyValue(SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY, true);
        socialDocumentFacetedNotePublic = session.createDocument(socialDocumentFacetedNotePublic);
        session.save();
        eventService.waitForAsyncCompletion();
        session.save();
        socialDocumentFacetedNotePublic = session.getDocument(socialDocumentFacetedNotePublic.getRef());

        socialDocument = toSocialDocument(socialDocumentFacetedNotePublic);
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        assertNull(socialDocument.getRestrictedDocument());
        checkPublic(socialDocument.getPublicDocument());
    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentPublic() throws Exception {
        DocumentModel privateNews = createSocialDocument(socialWorkspaceDoc.getPathAsString(), "private news",
                NEWS_ITEM_TYPE, true);

        SocialDocument socialDocument = toSocialDocument(privateNews);

        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());
    }

    @Test
    public void shouldNotCreatePrivateProxyForArticlePrivate() throws Exception {
        DocumentModel privateArticle = createSocialDocument(socialWorkspaceDoc.getPathAsString(), "privatenews",
                ARTICLE_TYPE, false);

        SocialDocument socialDocument = toSocialDocument(privateArticle);

        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

    }

    @Test
    public void shouldNotCreatePrivateProxyForArticlePublic() throws Exception {
        DocumentModel privateArticle = createSocialDocument(socialWorkspaceDoc.getPathAsString(), "privatenews",
                ARTICLE_TYPE, true);

        SocialDocument socialDocument = toSocialDocument(privateArticle);

        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());
    }

    protected void checkPublic(DocumentModel doc) throws ClientException {
        assertTrue(session.hasPermission(notMember, doc.getRef(), READ));
        assertFalse(session.hasPermission(notMember, doc.getRef(), READ_WRITE));
        assertTrue(session.hasPermission(member, doc.getRef(), READ_WRITE));
        assertTrue(session.hasPermission(administrator, doc.getRef(), EVERYTHING));
    }

    protected void checkRestricted(DocumentModel doc) throws ClientException {
        assertFalse(session.hasPermission(notMember, doc.getRef(), READ));
        assertTrue(session.hasPermission(member, doc.getRef(), READ_WRITE));
        assertTrue(session.hasPermission(administrator, doc.getRef(), EVERYTHING));
    }

}
