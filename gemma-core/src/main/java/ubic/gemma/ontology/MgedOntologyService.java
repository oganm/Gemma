/*
 * The GemmaOnt project
 * 
 * Copyright (c) 2007 University of British Columbia
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

package ubic.gemma.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

/**
 * Holds a complete copy of the MgedOntology in memory. This gets loaded on startup. As the MgedOntology is the
 * framework ontology i've added a feature so that the Ontology can be changed dynamically via the web front end.
 * 
 * @author klc
 * @version $Id: MgedOntologyService.java
 * @spring.bean id="mgedOntologyService"
 */

public class MgedOntologyService extends AbstractOntologyService {

    public static final String MGED_ONTO_BASE_URL = "http://mged.sourceforge.net/ontologies/MGEDOntology.owl";

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.ontology.AbstractOntologyService#getOntologyName()
     */
    @Override
    protected String getOntologyName() {
        return "mgedOntology";
    }

    protected static final Log log = LogFactory.getLog( MgedOntologyService.class );

    protected String ontology_startingPoint;

    public MgedOntologyService() {
        super();
        ontology_startingPoint = getOntologyStartingPoint();
    }

    public Collection<OntologyTreeNode> getBioMaterialTreeNodeTerms() {

        if ( !ready.get() ) return null;

        Collection<OntologyTreeNode> nodes = new ArrayList<OntologyTreeNode>();

        OntologyTerm term = terms.get( ontology_startingPoint );

        nodes.add( buildTreeNode( term ) );
        return nodes;
    }

    public Collection<OntologyTerm> getBioMaterialTerms() {

        if ( !ready.get() ) return null;

        OntologyTerm term = terms.get( ontology_startingPoint );
        Collection<OntologyTerm> results = getAllTerms( term );
        results.add( term );

        return results;

    }

    /**
     * Will attempt to load a different ontology into the MGED ontology service
     * 
     * @param ontologyURL
     * @param startingPointURL
     */
    public void loadNewOntology( String ontologyURL, String startingPointURL ) {

        if ( running.get() ) return;

        ontology_URL = ontologyURL;
        ontology_startingPoint = startingPointURL;

        ready = new AtomicBoolean( false );
        running = new AtomicBoolean( false );

        init();

    }

    protected Collection<OntologyTerm> getAllTerms( OntologyTerm term ) {

        Collection<OntologyTerm> children = term.getChildren( true );

        if ( ( children == null ) || ( children.isEmpty() ) ) return new HashSet<OntologyTerm>();

        Collection<OntologyTerm> grandChildren = new HashSet<OntologyTerm>();
        for ( OntologyTerm child : children ) {
            grandChildren.addAll( getAllTerms( child ) );
        }

        children.addAll( grandChildren );
        return children;

    }

    /**
     * @param node Recursivly builds the tree node structure that is needed by the ext tree
     */
    protected OntologyTreeNode buildTreeNode( OntologyTerm term ) {

        OntologyTreeNode node = new OntologyTreeNode( term );
        node.setLeaf( true );
        Collection<OntologyTerm> children = term.getChildren( true );

        if ( ( children != null ) && ( !children.isEmpty() ) ) {
            // node has children
            node.setAllowChildren( true );
            node.setLeaf( false );

            for ( OntologyTerm child : children ) {
                node.appendChild( buildTreeNode( child ) );
            }
        }

        return node;

    }

    @Override
    protected OntModel loadModel( String url, OntModelSpec spec ) throws IOException {
        return OntologyLoader.loadMemoryModel( url, spec );
    }

    protected String getOntologyStartingPoint() {
        return MGED_ONTO_BASE_URL + "#BioMaterialPackage";
    }

    @Override
    protected String getOntologyUrl() {
        return MGED_ONTO_BASE_URL;
    }

}