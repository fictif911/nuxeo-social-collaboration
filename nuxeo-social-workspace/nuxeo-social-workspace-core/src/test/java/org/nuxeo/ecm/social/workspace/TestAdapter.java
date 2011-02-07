package org.nuxeo.ecm.social.workspace;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import static org.junit.Assert.*;

import com.google.inject.Inject;


@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy( { "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.platform.dublincore",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.social.workspace.core"
        })
public class TestAdapter {

	@Inject
    protected CoreSession session;

	@Test
	public void testAdapter() throws Exception{
		DocumentModel article = session.createDocumentModel(session.getRootDocument().getPathAsString(), "article1", SocialConstants.ARTICLE_TYPE);
		assertNotNull(article);
		article = session.createDocument(article);
		session.save();


		ArticleAdapter adapter = article.getAdapter(ArticleAdapter.class);
		assertNotNull(adapter);
		assertNotNull(adapter.getCreated());
		System.out.println(">>>" +  adapter.getCreated());

	}


}
