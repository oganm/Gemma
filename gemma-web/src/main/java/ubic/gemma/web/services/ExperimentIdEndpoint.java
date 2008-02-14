/*
 * The Gemma project
 * 
 * Copyright (c) 2008 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ubic.gemma.web.services;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;

/**
 * Given the short name of an Expression Experiment, will return the matching Expression Experiment ID
 * @author klc, gavin
 * 
 */

public class ExperimentIdEndpoint extends AbstractGemmaEndpoint {

	private static Log log = LogFactory.getLog(ExperimentIdEndpoint.class);

	private ExpressionExperimentService expressionExperimentService;

	/**
	 * The local name of the request/response.
	 */
	public static final String EXPERIMENT_LOCAL_NAME = "experimentId";

	/**
	 * Sets the "business service" to delegate to.
	 */
	public void setExpressionExperimentService(ExpressionExperimentService ees) {
		this.expressionExperimentService = ees;
	}

	/**
	 * Reads the given <code>requestElement</code>, and sends a the response
	 * back.
	 * 
	 * @param requestElement
	 *            the contents of the SOAP message as DOM elements
	 * @param document
	 *            a DOM document to be used for constructing <code>Node</code>s
	 * @return the response element
	 */
	protected Element invokeInternal(Element requestElement, Document document)
			throws Exception {
		setLocalName(EXPERIMENT_LOCAL_NAME);
		String eeName ="";
		
		Collection<String> eeResults = getNodeValues(requestElement, "ee_short_name");
		
		for (String id: eeResults){
			eeName = id;
		}

		ExpressionExperiment ee = expressionExperimentService
				.findByShortName(eeName);

	
		//get Array Design ID and build results in the form of a collection
		Collection<String> eeId = new HashSet<String>();
		eeId.add(ee.getId().toString());
		
		

		return buildWrapper(document, eeId, "ee_id");

	}

}
