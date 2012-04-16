/*
 * The Gemma project
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
package ubic.gemma.association.phenotype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;

import ubic.basecode.ontology.model.OntologyTerm;
import ubic.gemma.association.phenotype.PhenotypeExceptions.EntityNotFoundException;
import ubic.gemma.genome.gene.service.GeneService;
import ubic.gemma.genome.taxon.service.TaxonService;
import ubic.gemma.loader.entrez.pubmed.PubMedXMLFetcher;
import ubic.gemma.model.association.phenotype.PhenotypeAssociation;
import ubic.gemma.model.association.phenotype.service.PhenotypeAssociationService;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.BibliographicReferenceService;
import ubic.gemma.model.common.description.BibliographicReferenceValueObject;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.CharacteristicService;
import ubic.gemma.model.common.description.DatabaseEntryDao;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.BibliographicPhenotypesValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.EvidenceSecurityValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.EvidenceValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.ExperimentalEvidenceValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.GeneEvidenceValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.LiteratureEvidenceValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.TreeCharacteristicValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.ValidateEvidenceValueObject;
import ubic.gemma.ontology.OntologyService;
import ubic.gemma.search.SearchResult;
import ubic.gemma.search.SearchService;
import ubic.gemma.search.SearchSettings;
import ubic.gemma.security.SecurityService;
import ubic.gemma.security.SecurityServiceImpl;
import ubic.gemma.security.authentication.UserManager;

/** High Level Service used to add Candidate Gene Management System capabilities */
@Service
public class PhenotypeAssociationManagerServiceImpl implements PhenotypeAssociationManagerService, InitializingBean {

    @Autowired
    private PhenotypeAssociationService associationService;

    @Autowired
    private PhenotypeAssoManagerServiceHelper phenotypeAssoManagerServiceHelper;

    @Autowired
    private OntologyService ontologyService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private TaxonService taxonService;

    @Autowired
    private CharacteristicService characteristicService;

    @Autowired
    private BibliographicReferenceService bibliographicReferenceService;

    @Autowired
    private DatabaseEntryDao databaseEntryDao;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private GeneService geneService;

    private PhenotypeAssoOntologyHelper ontologyHelper = null;
    private PubMedXMLFetcher pubMedXmlFetcher = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.ontologyHelper = new PhenotypeAssoOntologyHelper( this.ontologyService );
        this.pubMedXmlFetcher = new PubMedXMLFetcher();
    }

    /**
     * Links an Evidence to a Gene
     * 
     * @param geneNCBI The Gene NCBI we want to add the evidence
     * @param evidence The evidence
     * @return Status of the operation
     */
    @Override
    public ValidateEvidenceValueObject create( EvidenceValueObject evidence ) {

        if ( evidence.getPhenotypes().isEmpty() ) {
            throw new IllegalArgumentException( "Cannot create an Evidence with no Phenotype" );
        }
        if ( evidence.getGeneNCBI() == null ) {
            throw new IllegalArgumentException( "Cannot create an Evidence not linked to a Gene" );
        }

        ValidateEvidenceValueObject validateEvidenceValueObject = null;

        if ( isEvidenceAlreadyInDatabase( evidence ) ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setSameEvidenceFound( true );
            return validateEvidenceValueObject;
        }

        PhenotypeAssociation phenotypeAssociation = this.phenotypeAssoManagerServiceHelper
                .valueObject2Entity( evidence );

        phenotypeAssociation = this.associationService.create( phenotypeAssociation );

        Gene gene = phenotypeAssociation.getGene();
        gene.getPhenotypeAssociations().add( phenotypeAssociation );

        this.geneService.update( gene );

        return validateEvidenceValueObject;
    }

    /**
     * Return all evidence for a specific gene NCBI
     * 
     * @param geneNCBI The Evidence id
     * @return The Gene we are interested in
     */
    @Override
    public Collection<EvidenceValueObject> findEvidenceByGeneNCBI( Integer geneNCBI ) {

        Collection<PhenotypeAssociation> phenotypeAssociations = this.associationService
                .findPhenotypeAssociationForGeneNCBI( geneNCBI );

        return this.convert2ValueObjects( phenotypeAssociations );
    }

    /**
     * Return all evidence for a specific gene id
     * 
     * @param geneId The Evidence id
     * @return The Gene we are interested in
     */
    @Override
    public Collection<EvidenceValueObject> findEvidenceByGeneId( Long geneId ) {

        Collection<PhenotypeAssociation> phenotypeAssociations = this.associationService
                .findPhenotypeAssociationForGeneId( geneId );

        return this.convert2ValueObjects( phenotypeAssociations );
    }

    /**
     * Return all evidence for a specific gene id with evidence flagged, indicating more information
     * 
     * @param geneId The Evidence id
     * @param phenotypesValuesUri the chosen phenotypes
     * @return The Gene we are interested in
     */
    @Override
    public Collection<EvidenceValueObject> findEvidenceByGeneId( Long geneId, Set<String> phenotypesValuesUri ) {

        Collection<EvidenceValueObject> evidenceValueObjects = findEvidenceByGeneId( geneId );

        flagEvidence( evidenceValueObjects, phenotypesValuesUri );

        return evidenceValueObjects;
    }

    /**
     * Given a set of phenotypes returns the genes that have all those phenotypes or children phenotypes
     * 
     * @param phenotypesValuesUri the roots phenotype of the query
     * @return A collection of the genes found
     */
    @Override
    public Collection<GeneValueObject> findCandidateGenes( Set<String> phenotypesValuesUri ) {

        if ( phenotypesValuesUri == null || phenotypesValuesUri.isEmpty() ) {
            throw new IllegalArgumentException( "No phenotypes values uri provided" );
        }

        // map query phenotypes given to the set of possible children phenotypes in the database + query phenotype
        HashMap<String, Set<String>> phenotypesWithChildren = findChildrenForEachPhenotypes( phenotypesValuesUri );

        Set<String> possibleChildrenPhenotypes = new HashSet<String>();

        for ( String key : phenotypesWithChildren.keySet() ) {
            possibleChildrenPhenotypes.addAll( phenotypesWithChildren.get( key ) );
        }

        // find all Genes containing the first phenotypeValueUri
        Collection<Gene> genes = this.associationService.findGeneWithPhenotypes( possibleChildrenPhenotypes );

        // dont keep genes with evidence that the user doesnt have the permissions to see
        Collection<Gene> genesAfterAcl = filterGeneAfterAcl( genes );

        Collection<GeneValueObject> genesVO = null;

        if ( phenotypesValuesUri.size() == 1 ) {
            genesVO = GeneValueObject.convert2ValueObjects( genesAfterAcl );
        }
        // we received a set of Gene with the first phenotype, we need to filter this set and keep only genes that have
        // all root phenotypes or their children
        else {
            genesVO = filterGenesWithPhenotypes( genesAfterAcl, phenotypesWithChildren );
        }

        return genesVO;
    }

    /**
     * This method is a temporary solution, we will be using findAllPhenotypesByTree() directly in the future
     * 
     * @return A collection of the phenotypes with the gene occurence
     */
    @Override
    public Collection<CharacteristicValueObject> loadAllPhenotypes() {

        Collection<CharacteristicValueObject> characteristcsVO = new TreeSet<CharacteristicValueObject>();

        // load the tree
        Collection<TreeCharacteristicValueObject> treeCharacteristicValueObject = findAllPhenotypesByTree();

        // undo the tree in a simple structure
        for ( TreeCharacteristicValueObject t : treeCharacteristicValueObject ) {
            addChildren( characteristcsVO, t );
        }

        return characteristcsVO;
    }

    /**
     * Removes an evidence
     * 
     * @param id The Evidence database id
     */
    @Override
    public ValidateEvidenceValueObject remove( Long id ) {

        ValidateEvidenceValueObject validateEvidenceValueObject = null;

        PhenotypeAssociation evidence = this.associationService.load( id );

        if ( evidence != null ) {

            if ( evidence.getEvidenceSource() != null ) {
                this.databaseEntryDao.remove( evidence.getEvidenceSource().getId() );
            }

            this.associationService.remove( evidence );

        } else {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setEvidenceNotFound( true );
        }
        return validateEvidenceValueObject;
    }

    /**
     * Load an evidence
     * 
     * @param id The Evidence database id
     */
    @Override
    public EvidenceValueObject load( Long id ) {

        PhenotypeAssociation phenotypeAssociation = this.associationService.load( id );
        EvidenceValueObject evidenceValueObject = EvidenceValueObject.convert2ValueObjects( phenotypeAssociation );
        if ( evidenceValueObject != null ) {
            findEvidencePermissions( phenotypeAssociation, evidenceValueObject );
        }
        return evidenceValueObject;
    }

    /**
     * Modify an existing evidence
     * 
     * @param evidenceValueObject the evidence with modified fields
     * @return Status of the operation
     */
    @Override
    public ValidateEvidenceValueObject update( EvidenceValueObject modifedEvidenceValueObject ) {

        ValidateEvidenceValueObject validateEvidenceValueObject = null;

        if ( modifedEvidenceValueObject.getPhenotypes() == null || modifedEvidenceValueObject.getPhenotypes().isEmpty() ) {
            throw new IllegalArgumentException( "An evidence cannot have no phenotype" );
        }

        if ( modifedEvidenceValueObject.getGeneNCBI() == null ) {
            throw new IllegalArgumentException( "Evidence not linked to a Gene" );
        }

        if ( modifedEvidenceValueObject.getId() == null ) {
            throw new IllegalArgumentException( "No database id provided" );
        }

        if ( isEvidenceAlreadyInDatabase( modifedEvidenceValueObject ) ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setSameEvidenceFound( true );
            return validateEvidenceValueObject;
        }

        PhenotypeAssociation phenotypeAssociation = this.associationService.load( modifedEvidenceValueObject.getId() );

        if ( phenotypeAssociation == null ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setEvidenceNotFound( true );
            return validateEvidenceValueObject;
        }

        // check for the race condition
        if ( phenotypeAssociation.getStatus().getLastUpdateDate().getTime() != modifedEvidenceValueObject
                .getLastUpdated() ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setLastUpdateDifferent( true );
            return validateEvidenceValueObject;
        }

        EvidenceValueObject evidenceValueObject = EvidenceValueObject.convert2ValueObjects( phenotypeAssociation );

        // evidence type changed
        if ( !evidenceValueObject.getClass().equals( modifedEvidenceValueObject.getClass() ) ) {
            remove( modifedEvidenceValueObject.getId() );
            return create( modifedEvidenceValueObject );
        }

        // modify phenotypes
        populateModifiedPhenotypes( modifedEvidenceValueObject.getPhenotypes(), phenotypeAssociation );

        // modify all other values needed
        this.phenotypeAssoManagerServiceHelper
                .populateModifiedValues( modifedEvidenceValueObject, phenotypeAssociation );

        this.associationService.update( phenotypeAssociation );

        return validateEvidenceValueObject;
    }

    /**
     * Giving a phenotype searchQuery, returns a selection choice to the user
     * 
     * @param searchQuery query typed by the user
     * @param geneId the id of the chosen gene
     * @return Collection<CharacteristicValueObject> list of choices returned
     */
    @Override
    public Collection<CharacteristicValueObject> searchOntologyForPhenotypes( String searchQuery, Long geneId ) {

        ArrayList<CharacteristicValueObject> orderedPhenotypesFromOntology = new ArrayList<CharacteristicValueObject>();

        boolean geneProvided = true;

        if ( geneId == null ) {
            geneProvided = false;
        }

        // prepare the searchQuery to correctly query the Ontology
        String newSearchQuery = prepareOntologyQuery( searchQuery );

        // search the Ontology with the new search query
        Set<CharacteristicValueObject> allPhenotypesFoundInOntology = this.ontologyHelper
                .findPhenotypesInOntology( newSearchQuery );

        // All phenotypes present on the gene (if the gene was given)
        Set<CharacteristicValueObject> phenotypesOnCurrentGene = null;

        if ( geneProvided ) {
            phenotypesOnCurrentGene = findUniquePhenotypesForGeneId( geneId );
        }

        // all phenotypes currently in the database
        Set<String> allPhenotypesInDatabase = this.associationService.loadAllPhenotypesUri();

        // rules to order the Ontology results found
        Collection<CharacteristicValueObject> phenotypesWithExactMatch = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesAlreadyPresentOnGene = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesStartWithQueryAndInDatabase = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesStartWithQuery = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesSubstringAndInDatabase = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesSubstring = new ArrayList<CharacteristicValueObject>();
        Collection<CharacteristicValueObject> phenotypesNoRuleFound = new ArrayList<CharacteristicValueObject>();

        /*
         * for each CharacteristicVO found from the Ontology, filter them and add them to a specific list if they
         * satisfied the condition
         */
        for ( CharacteristicValueObject cha : allPhenotypesFoundInOntology ) {

            // set flag for UI, flag if the phenotype is on the Gene or if in the database
            if ( phenotypesOnCurrentGene != null && phenotypesOnCurrentGene.contains( cha ) ) {
                cha.setAlreadyPresentOnGene( true );
            } else if ( allPhenotypesInDatabase.contains( cha.getValueUri() ) ) {
                cha.setAlreadyPresentInDatabase( true );
            }

            // order the results by specific rules

            // Case 1, exact match
            if ( cha.getValue().equalsIgnoreCase( searchQuery ) ) {
                phenotypesWithExactMatch.add( cha );
            }
            // Case 2, phenotype already present on Gene
            else if ( phenotypesOnCurrentGene != null && phenotypesOnCurrentGene.contains( cha ) ) {
                phenotypesAlreadyPresentOnGene.add( cha );
            }
            // Case 3, starts with a substring of the word
            else if ( cha.getValue().toLowerCase().startsWith( searchQuery.toLowerCase() ) ) {
                if ( allPhenotypesInDatabase.contains( cha.getValueUri() ) ) {
                    phenotypesStartWithQueryAndInDatabase.add( cha );
                } else {
                    phenotypesStartWithQuery.add( cha );
                }
            }
            // Case 4, contains a substring of the word
            else if ( cha.getValue().toLowerCase().indexOf( searchQuery.toLowerCase() ) != -1 ) {
                if ( allPhenotypesInDatabase.contains( cha.getValueUri() ) ) {
                    phenotypesSubstringAndInDatabase.add( cha );
                } else {
                    phenotypesSubstring.add( cha );
                }
            } else {
                phenotypesNoRuleFound.add( cha );
            }
        }

        // place them in the correct order to display
        orderedPhenotypesFromOntology.addAll( phenotypesWithExactMatch );
        orderedPhenotypesFromOntology.addAll( phenotypesAlreadyPresentOnGene );
        orderedPhenotypesFromOntology.addAll( phenotypesStartWithQueryAndInDatabase );
        orderedPhenotypesFromOntology.addAll( phenotypesSubstringAndInDatabase );
        orderedPhenotypesFromOntology.addAll( phenotypesStartWithQuery );
        orderedPhenotypesFromOntology.addAll( phenotypesSubstring );
        orderedPhenotypesFromOntology.addAll( phenotypesNoRuleFound );

        // limit the size of the returned phenotypes to 100 terms
        if ( orderedPhenotypesFromOntology.size() > 100 ) {
            return orderedPhenotypesFromOntology.subList( 0, 100 );
        }

        return orderedPhenotypesFromOntology;
    }

    /**
     * Using all the phenotypes in the database, builds a tree structure using the Ontology
     * 
     * @return Collection<TreeCharacteristicValueObject> list of all phenotypes in gemma represented as trees
     */
    @Override
    public Collection<TreeCharacteristicValueObject> findAllPhenotypesByTree() {

        Collection<TreeCharacteristicValueObject> treesPhenotypes = buildTree();

        Collection<TreeCharacteristicValueObject> finalTree = new TreeSet<TreeCharacteristicValueObject>();

        String username = null;

        if ( SecurityServiceImpl.isUserLoggedIn() ) {
            // find user
            username = this.userManager.getCurrentUsername();
            // TODO find also groups
        }

        for ( TreeCharacteristicValueObject tc : treesPhenotypes ) {

            // count occurrence recursively for each phenotype in the branch
            tc.countGeneOccurence( this.associationService, SecurityServiceImpl.isUserAdmin(), username );
            if ( tc.getPublicGeneCount() + tc.getPrivateGeneCount() != 0 ) {
                finalTree.add( tc );
            }
        }

        return finalTree;
    }

    /**
     * Does a Gene search (by name or symbol) for a query and return only Genes with evidence
     * 
     * @param query
     * @param taxonId, can be null to not constrain by taxon
     * @return Collection<GeneEvidenceValueObject> list of Genes
     */
    @Override
    public Collection<GeneEvidenceValueObject> findGenesWithEvidence( String query, Long taxonId ) {

        if ( query == null || query.length() == 0 ) {
            throw new IllegalArgumentException( "No search query provided" );
        }

        // make sure it does an inexact search
        String newQuery = query + "%";

        Taxon taxon = null;
        if ( taxonId != null ) {
            taxon = this.taxonService.load( taxonId );
        }
        SearchSettings settings = SearchSettings.geneSearch( newQuery, taxon );
        List<SearchResult> geneSearchResults = this.searchService.search( settings ).get( Gene.class );

        Collection<Gene> genes = new HashSet<Gene>();
        if ( geneSearchResults == null || geneSearchResults.isEmpty() ) {
            return new HashSet<GeneEvidenceValueObject>();
        }

        for ( SearchResult sr : geneSearchResults ) {
            genes.add( ( Gene ) sr.getResultObject() );
        }

        Collection<GeneEvidenceValueObject> geneEvidenceValueObjects = GeneEvidenceValueObject
                .convert2GeneEvidenceValueObjects( genes );

        Collection<GeneEvidenceValueObject> geneValueObjectsFilter = new ArrayList<GeneEvidenceValueObject>();

        for ( GeneEvidenceValueObject gene : geneEvidenceValueObjects ) {
            if ( gene.getEvidence() != null && gene.getEvidence().size() != 0 ) {
                geneValueObjectsFilter.add( gene );
            }
        }

        return geneValueObjectsFilter;
    }

    /**
     * Find all phenotypes associated to a pubmedID
     * 
     * @param pubMedId
     * @param evidenceId optional, used if we are updating to know current annotation
     * @return BibliographicReferenceValueObject
     */
    @Override
    public BibliographicReferenceValueObject findBibliographicReference( String pubMedId, Long evidenceId ) {

        // check if the given pubmedID is already in the database
        BibliographicReference bibliographicReference = this.bibliographicReferenceService.findByExternalId( pubMedId );

        // already in the database
        if ( bibliographicReference != null ) {

            BibliographicReferenceValueObject bibliographicReferenceVO = new BibliographicReferenceValueObject(
                    bibliographicReference );

            Collection<PhenotypeAssociation> phenotypeAssociations = this.associationService
                    .findPhenotypesForBibliographicReference( pubMedId );

            Collection<BibliographicPhenotypesValueObject> bibliographicPhenotypesValueObjects = BibliographicPhenotypesValueObject
                    .phenotypeAssociations2BibliographicPhenotypesValueObjects( phenotypeAssociations );

            // set phenotypes associated with this bibliographic reference
            bibliographicReferenceVO.setBibliographicPhenotypes( bibliographicPhenotypesValueObjects );

            // set experiments associated with this bibliographic reference
            Collection<ExpressionExperiment> experiments = this.bibliographicReferenceService
                    .getRelatedExperiments( bibliographicReference );

            if ( experiments != null && !experiments.isEmpty() ) {
                bibliographicReferenceVO.setExperiments( ExpressionExperimentValueObject
                        .convert2ValueObjects( experiments ) );
            }

            return bibliographicReferenceVO;
        }

        // find the Bibliographic on PubMed
        bibliographicReference = this.pubMedXmlFetcher.retrieveByHTTP( Integer.parseInt( pubMedId ) );

        // the pudmedId doesn't exists in PudMed
        if ( bibliographicReference == null ) {
            return null;
        }

        BibliographicReferenceValueObject bibliographicReferenceValueObject = new BibliographicReferenceValueObject(
                bibliographicReference );

        // we are in an update, we want to know which phenotypes from the literature are on this evidence
        if ( evidenceId != null ) {
            // evidence being updated, original from the database
            EvidenceValueObject evidence = EvidenceValueObject.convert2ValueObjects( this.associationService
                    .load( evidenceId ) );
            Set<CharacteristicValueObject> allPhenotypeOnEvidence = evidence.getPhenotypes();

            for ( BibliographicPhenotypesValueObject bibliographicPhenotypesValueObject : bibliographicReferenceValueObject
                    .getBibliographicPhenotypes() ) {

                if ( allPhenotypeOnEvidence.equals( bibliographicPhenotypesValueObject.getPhenotypesValues() ) ) {
                    bibliographicPhenotypesValueObject.setOriginalPhenotype( true );
                }
            }
        }
        return bibliographicReferenceValueObject;
    }

    /**
     * Validate an Evidence before we create it
     * 
     * @param geneNCBI The Gene NCBI we want to add the evidence
     * @param evidence The evidence
     * @return ValidateEvidenceValueObject flags of information to show user messages
     */
    @Override
    public ValidateEvidenceValueObject validateEvidence( EvidenceValueObject evidence ) {

        ValidateEvidenceValueObject validateEvidenceValueObject = null;

        if ( isEvidenceAlreadyInDatabase( evidence ) ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setSameEvidenceFound( true );
            return validateEvidenceValueObject;
        }

        if ( evidence instanceof LiteratureEvidenceValueObject ) {

            String pubmedId = ( ( LiteratureEvidenceValueObject ) evidence ).getCitationValueObject()
                    .getPubmedAccession();

            validateEvidenceValueObject = determineSameGeneAndPhenotypeAnnotated( evidence, pubmedId );

        } else if ( evidence instanceof ExperimentalEvidenceValueObject ) {

            ExperimentalEvidenceValueObject experimentalEvidenceValueObject = ( ExperimentalEvidenceValueObject ) evidence;

            if ( experimentalEvidenceValueObject.getPrimaryPublicationCitationValueObject() != null ) {

                String pubmedId = experimentalEvidenceValueObject.getPrimaryPublicationCitationValueObject()
                        .getPubmedAccession();
                validateEvidenceValueObject = determineSameGeneAndPhenotypeAnnotated( evidence, pubmedId );
            }
        }
        return validateEvidenceValueObject;
    }

    /**
     * Find mged category term that were used in the database, used to annotated Experiments
     * 
     * @return Collection<CharacteristicValueObject> the terms found
     */
    @Override
    public Collection<CharacteristicValueObject> findExperimentMgedCategory() {
        return this.associationService.findEvidenceMgedCategoryTerms();
    }

    /**
     * for a given search string look in the database and Ontology for matches
     * 
     * @param givenQueryString the search query
     * @param categoryUri the mged category (can be null)
     * @param taxonId the taxon id (can be null)
     * @return Collection<CharacteristicValueObject> the terms found
     */
    @Override
    public Collection<CharacteristicValueObject> findExperimentOntologyValue( String givenQueryString,
            String categoryUri, Long taxonId ) {

        Taxon taxon = null;
        if ( taxonId != null ) {
            taxon = this.taxonService.load( taxonId );
        }

        return this.ontologyService.findExactTermValueObject( givenQueryString, categoryUri, taxon );
    }

    @Override
    public void setOntologyHelper( PhenotypeAssoOntologyHelper ontologyHelper ) {
        this.ontologyHelper = ontologyHelper;
        this.phenotypeAssoManagerServiceHelper.setOntologyHelper( this.ontologyHelper );
    }

    /**
     * this method can be used if we want to reimport data from a specific external Database, this method will remove
     * from the database ALL evidences link to the given external database
     * 
     * @param externalDatabaseName
     */
    @Override
    public void removeEvidencesWithExternalDatabaseName( String externalDatabaseName ) {
        Collection<PhenotypeAssociation> phenotypeAssociations = this.associationService
                .findEvidencesWithExternalDatabaseName( externalDatabaseName );

        for ( PhenotypeAssociation phenotypeAssociation : phenotypeAssociations ) {
            remove( phenotypeAssociation.getId() );
        }
    }

    /** Given a geneId finds all phenotypes for that gene */
    private Set<CharacteristicValueObject> findUniquePhenotypesForGeneId( Long geneId ) {

        Set<CharacteristicValueObject> phenotypes = new TreeSet<CharacteristicValueObject>();

        Collection<EvidenceValueObject> evidence = findEvidenceByGeneId( geneId );

        for ( EvidenceValueObject evidenceVO : evidence ) {
            phenotypes.addAll( evidenceVO.getPhenotypes() );
        }
        return phenotypes;
    }

    /** This method is a temporary solution, we will be using findAllPhenotypesByTree() directly in the future */
    private void addChildren( Collection<CharacteristicValueObject> characteristcsVO, TreeCharacteristicValueObject t ) {

        CharacteristicValueObject cha = new CharacteristicValueObject( t.getValue().toLowerCase(), t.getCategory(),
                t.getValueUri(), t.getCategoryUri() );

        cha.setPublicGeneCount( t.getPublicGeneCount() );
        cha.setPrivateGeneCount( t.getPrivateGeneCount() );

        characteristcsVO.add( cha );

        for ( TreeCharacteristicValueObject tree : t.getChildren() ) {
            addChildren( characteristcsVO, tree );
        }
    }

    private Collection<TreeCharacteristicValueObject> buildTree() {

        // represents each phenotype and childs found in the Ontology, TreeSet used to order trees
        TreeSet<TreeCharacteristicValueObject> treesPhenotypes = new TreeSet<TreeCharacteristicValueObject>();

        // all phenotypes in Gemma
        Set<CharacteristicValueObject> allPhenotypes = this.associationService.loadAllPhenotypes();

        // keep track of all phenotypes found in the trees, used to find quickly the position to add subtrees
        HashMap<String, TreeCharacteristicValueObject> phenotypeFoundInTree = new HashMap<String, TreeCharacteristicValueObject>();

        // for each phenotype in Gemma construct its subtree of children if necessary
        for ( CharacteristicValueObject c : allPhenotypes ) {

            // dont create the tree if it is already present in an other
            if ( phenotypeFoundInTree.get( c.getValueUri() ) != null ) {
                // flag the node as phenotype found in database
                phenotypeFoundInTree.get( c.getValueUri() ).setDbPhenotype( true );

            } else {

                try {
                    // find the ontology term using the valueURI
                    OntologyTerm ontologyTerm = this.ontologyHelper.findOntologyTermByUri( c.getValueUri() );

                    // transform an OntologyTerm and his children to a TreeCharacteristicValueObject
                    TreeCharacteristicValueObject treeCharacteristicValueObject = TreeCharacteristicValueObject
                            .ontology2TreeCharacteristicValueObjects( ontologyTerm, phenotypeFoundInTree,
                                    treesPhenotypes );

                    // set flag that this node represents a phenotype in the database
                    treeCharacteristicValueObject.setDbPhenotype( true );

                    // add tree to the phenotypes found in ontology
                    phenotypeFoundInTree.put( ontologyTerm.getUri(), treeCharacteristicValueObject );

                    treesPhenotypes.add( treeCharacteristicValueObject );

                } catch ( EntityNotFoundException entityNotFoundException ) {
                    System.err.println( "A valueUri found in the database was not found in the ontology" );
                    System.err.println( "This can happen when a valueUri is updated in the ontology" );
                    System.err.println( "Value : " + c.getValue() + "      valueUri: " + c.getValueUri() );
                }
            }
        }
        // remove all nodes in the trees found in the Ontology but not in the database
        for ( TreeCharacteristicValueObject tc : treesPhenotypes ) {
            tc.removeUnusedPhenotypes( tc.getValueUri() );
        }

        return treesPhenotypes;
    }

    /** map query phenotypes given to the set of possible children phenotypes in the database */
    private HashMap<String, Set<String>> findChildrenForEachPhenotypes( Set<String> phenotypesValuesUri ) {

        // root corresponds to one value found in phenotypesValuesUri
        // root ---> root+children phenotypes
        HashMap<String, Set<String>> parentPheno = new HashMap<String, Set<String>>();

        Set<String> phenotypesUriInDatabase = this.associationService.loadAllPhenotypesUri();

        // determine all children terms for each other phenotypes
        for ( String phenoRoot : phenotypesValuesUri ) {

            OntologyTerm ontologyTermFound = this.ontologyHelper.findOntologyTermByUri( phenoRoot );
            Collection<OntologyTerm> ontologyChildrenFound = ontologyTermFound.getChildren( false );

            Set<String> parentChildren = new HashSet<String>();
            parentChildren.add( phenoRoot );

            for ( OntologyTerm ot : ontologyChildrenFound ) {

                if ( phenotypesUriInDatabase.contains( ot.getUri() ) ) {
                    parentChildren.add( ot.getUri() );
                }
            }
            parentPheno.put( phenoRoot, parentChildren );
        }
        return parentPheno;
    }

    /** Filter a set of genes if who have the root phenotype or a children of a root phenotype */
    private Collection<GeneValueObject> filterGenesWithPhenotypes( Collection<Gene> genes,
            HashMap<String, Set<String>> phenotypesWithChildren ) {

        Collection<GeneValueObject> genesVO = new HashSet<GeneValueObject>();

        for ( Gene gene : genes ) {

            // all phenotypeUri for a gene
            Set<String> allPhenotypesOnGene = findAllPhenotpyesOnGene( gene );

            // if the Gene has all the phenotypes
            boolean keepGene = true;

            for ( String phe : phenotypesWithChildren.keySet() ) {

                // at least 1 value must be found
                Set<String> possiblePheno = phenotypesWithChildren.get( phe );

                boolean foundSpecificPheno = false;

                for ( String pheno : possiblePheno ) {

                    if ( allPhenotypesOnGene.contains( pheno ) ) {
                        foundSpecificPheno = true;
                    }
                }

                if ( foundSpecificPheno == false ) {
                    // dont keep the gene since a root phenotype + children was not found for all evidence of that gene
                    keepGene = false;
                    break;
                }
            }
            if ( keepGene ) {
                GeneValueObject geneValueObject = new GeneValueObject( gene );
                genesVO.add( geneValueObject );
            }
        }

        return genesVO;
    }

    /** add flags to Evidence and CharacteristicvalueObjects */
    private void flagEvidence( Collection<EvidenceValueObject> evidencesVO, Set<String> phenotypesValuesUri ) {

        // map query phenotypes given to the set of possible children phenotypes in the database + query phenotype
        HashMap<String, Set<String>> phenotypesWithChildren = findChildrenForEachPhenotypes( phenotypesValuesUri );

        Set<String> possibleChildrenPhenotypes = new HashSet<String>();

        for ( String key : phenotypesWithChildren.keySet() ) {
            possibleChildrenPhenotypes.addAll( phenotypesWithChildren.get( key ) );
        }

        // flag relevant evidence, root phenotypes and children phenotypes

        for ( EvidenceValueObject evidenceVO : evidencesVO ) {

            boolean relevantEvidence = false;

            for ( CharacteristicValueObject chaVO : evidenceVO.getPhenotypes() ) {

                // if the phenotype is a root
                if ( phenotypesValuesUri.contains( chaVO.getValueUri() ) ) {
                    relevantEvidence = true;
                    chaVO.setRoot( true );
                }
                // if the phenotype is a children of the root
                else if ( possibleChildrenPhenotypes.contains( chaVO.getValueUri() ) ) {
                    chaVO.setChild( true );
                    relevantEvidence = true;
                }
            }
            if ( relevantEvidence ) {
                evidenceVO.setRelevance( new Double( 1.0 ) );
            }
        }

    }

    /** change a searchQuery to make it search in the Ontology using * and AND */
    private String prepareOntologyQuery( String searchQuery ) {
        String newSearchQuery = searchQuery.trim().replaceAll( "\\s+", "* " ) + "*";
        return StringUtils.join( newSearchQuery.split( " " ), " AND " );
    }

    /**
     * Convert an collection of evidence entities to their corresponding value objects
     * 
     * @param phenotypeAssociations The List of entities we need to convert to value object
     * @return Collection<EvidenceValueObject> the converted results
     */
    private Collection<EvidenceValueObject> convert2ValueObjects( Collection<PhenotypeAssociation> phenotypeAssociations ) {

        Collection<EvidenceValueObject> returnEvidenceVO = new HashSet<EvidenceValueObject>();

        if ( phenotypeAssociations != null ) {

            for ( PhenotypeAssociation phe : phenotypeAssociations ) {

                EvidenceValueObject evidence = EvidenceValueObject.convert2ValueObjects( phe );
                findEvidencePermissions( phe, evidence );

                if ( evidence != null ) {
                    returnEvidenceVO.add( evidence );
                }
            }
        }
        return returnEvidenceVO;
    }

    /** determine permissions for an PhenotypeAssociation */
    private void findEvidencePermissions( PhenotypeAssociation p, EvidenceValueObject evidenceValueObject ) {

        Boolean currentUserHasWritePermission = false;
        String owner = null;
        Boolean isPublic = this.securityService.isPublic( p );
        Boolean isShared = this.securityService.isShared( p );
        Boolean currentUserIsOwner = this.securityService.isOwnedByCurrentUser( p );

        if ( currentUserIsOwner || isPublic || isShared ) {

            currentUserHasWritePermission = this.securityService.isEditable( p );
            owner = ( ( PrincipalSid ) this.securityService.getOwner( p ) ).getPrincipal();
        }

        evidenceValueObject.setEvidenceSecurityValueObject( new EvidenceSecurityValueObject(
                currentUserHasWritePermission, currentUserIsOwner, isPublic, isShared, owner ) );
    }

    /** take care of populating new values for the phenotypes in an update */
    private void populateModifiedPhenotypes( Set<CharacteristicValueObject> updatedPhenotypes,
            PhenotypeAssociation phenotypeAssociation ) {

        // the modified final phenotype to update
        Collection<Characteristic> finalPhenotypes = new HashSet<Characteristic>();

        HashMap<Long, CharacteristicValueObject> updatedPhenotypesMap = new HashMap<Long, CharacteristicValueObject>();

        for ( CharacteristicValueObject updatedPhenotype : updatedPhenotypes ) {

            // updated
            if ( updatedPhenotype.getId() != 0 ) {
                updatedPhenotypesMap.put( updatedPhenotype.getId(), updatedPhenotype );
            }
            // new one
            else {
                finalPhenotypes.add( this.ontologyHelper.valueUri2Characteristic( updatedPhenotype.getValueUri() ) );
            }
        }

        for ( Characteristic cha : phenotypeAssociation.getPhenotypes() ) {

            VocabCharacteristic phenotype = ( VocabCharacteristic ) cha;

            CharacteristicValueObject updatedPhenotype = updatedPhenotypesMap.get( phenotype.getId() );

            // found an update, same database id
            if ( updatedPhenotype != null ) {

                // same values as before
                if ( updatedPhenotype.equals( phenotype ) ) {
                    finalPhenotypes.add( phenotype );
                } else {
                    // different values found
                    phenotype.setValueUri( updatedPhenotype.getValueUri() );
                    phenotype.setValue( updatedPhenotype.getValue() );
                    finalPhenotypes.add( phenotype );
                }
            }
            // this phenotype was deleted
            else {
                this.characteristicService.delete( cha.getId() );
            }
        }
        phenotypeAssociation.getPhenotypes().clear();
        phenotypeAssociation.getPhenotypes().addAll( finalPhenotypes );
    }

    /** return a collection of Gene that the user is allowed to see */
    private Collection<Gene> filterGeneAfterAcl( Collection<Gene> genes ) {

        Collection<Gene> genesAfterAcl = new HashSet<Gene>();

        for ( Gene gene : genes ) {
            for ( PhenotypeAssociation phenotypeAssociation : gene.getPhenotypeAssociations() ) {

                if ( this.securityService.isPublic( phenotypeAssociation ) ) {
                    genesAfterAcl.add( gene );
                    break;
                } else if ( this.securityService.isShared( phenotypeAssociation ) ) {
                    genesAfterAcl.add( gene );
                    break;
                } else if ( this.securityService.isOwnedByCurrentUser( phenotypeAssociation ) ) {
                    genesAfterAcl.add( gene );
                    break;
                }
            }
        }
        return genesAfterAcl;
    }

    /** get a Set of all phenotypes present ona gene */
    private Set<String> findAllPhenotpyesOnGene( Gene gene ) {

        Set<String> allPhenotypesOnGene = new HashSet<String>();

        for ( PhenotypeAssociation p : gene.getPhenotypeAssociations() ) {
            for ( Characteristic cha : p.getPhenotypes() ) {
                allPhenotypesOnGene.add( ( ( VocabCharacteristic ) ( cha ) ).getValueUri() );
            }
        }
        return allPhenotypesOnGene;
    }

    /** populates the ValidateEvidenceValueObject with the correct flags if necessary */
    private ValidateEvidenceValueObject determineSameGeneAndPhenotypeAnnotated( EvidenceValueObject evidence,
            String pubmed ) {

        ValidateEvidenceValueObject validateEvidenceValueObject = null;

        BibliographicReferenceValueObject bibliographicReferenceValueObject = findBibliographicReference( pubmed,
                evidence.getId() );

        if ( bibliographicReferenceValueObject == null ) {
            validateEvidenceValueObject = new ValidateEvidenceValueObject();
            validateEvidenceValueObject.setPubmedIdInvalid( true );
        } else {

            // rule to determine if its an Update
            if ( evidence.getId() != null ) {

                PhenotypeAssociation phenotypeAssociation = this.associationService.load( evidence.getId() );

                if ( phenotypeAssociation == null ) {
                    validateEvidenceValueObject = new ValidateEvidenceValueObject();
                    validateEvidenceValueObject.setEvidenceNotFound( true );
                    return validateEvidenceValueObject;
                }

                // check for the race condition
                if ( phenotypeAssociation.getStatus().getLastUpdateDate().getTime() != evidence.getLastUpdated() ) {
                    validateEvidenceValueObject = new ValidateEvidenceValueObject();
                    validateEvidenceValueObject.setLastUpdateDifferent( true );
                    return validateEvidenceValueObject;
                }
            }

            for ( BibliographicPhenotypesValueObject bibliographicPhenotypesValueObject : bibliographicReferenceValueObject
                    .getBibliographicPhenotypes() ) {

                if ( evidence.getId() != null ) {
                    // dont compare evidence to itself since it already exists
                    if ( evidence.getId().equals( bibliographicPhenotypesValueObject.getEvidenceId() ) ) {
                        continue;
                    }
                }

                // look if the gene have already been annotated
                if ( evidence.getGeneNCBI().equals( bibliographicPhenotypesValueObject.getGeneNCBI() ) ) {

                    if ( validateEvidenceValueObject == null ) {
                        validateEvidenceValueObject = new ValidateEvidenceValueObject();
                        validateEvidenceValueObject
                                .setBibliographicReferenceValueObject( bibliographicReferenceValueObject );
                    }

                    validateEvidenceValueObject.setSameGeneAnnotated( true );
                    bibliographicPhenotypesValueObject.setToHighlight( true );

                    boolean containsExact = true;

                    for ( CharacteristicValueObject phenotype : evidence.getPhenotypes() ) {

                        if ( !bibliographicPhenotypesValueObject.getPhenotypesValues().contains( phenotype ) ) {
                            containsExact = false;
                        }
                    }

                    if ( containsExact ) {
                        validateEvidenceValueObject.setSameGeneAndOnePhenotypeAnnotated( true );
                        bibliographicPhenotypesValueObject.setToHighlight( true );
                    }

                    if ( evidence.getPhenotypes().size() == bibliographicPhenotypesValueObject.getPhenotypesValues()
                            .size()
                            && evidence.getPhenotypes().containsAll(
                                    bibliographicPhenotypesValueObject.getPhenotypesValues() ) ) {
                        validateEvidenceValueObject.setSameGeneAndPhenotypesAnnotated( true );
                        bibliographicPhenotypesValueObject.setToHighlight( true );
                    }

                    Set<String> parentOrChildTerm = new HashSet<String>();

                    // for the phenotype already present we add his children and direct parents, and check that
                    // the phenotype we want to add is not in that subset
                    for ( CharacteristicValueObject phenotypeAlreadyPresent : bibliographicPhenotypesValueObject
                            .getPhenotypesValues() ) {

                        OntologyTerm ontologyTerm = this.ontologyService
                                .getTerm( phenotypeAlreadyPresent.getValueUri() );

                        for ( OntologyTerm ot : ontologyTerm.getParents( true ) ) {
                            parentOrChildTerm.add( ot.getUri() );
                        }

                        for ( OntologyTerm ot : ontologyTerm.getChildren( false ) ) {
                            parentOrChildTerm.add( ot.getUri() );
                        }
                    }

                    for ( CharacteristicValueObject characteristicValueObject : evidence.getPhenotypes() ) {

                        if ( parentOrChildTerm.contains( characteristicValueObject.getValueUri() ) ) {
                            validateEvidenceValueObject.setSameGeneAndPhenotypeChildOrParentAnnotated( true );
                            bibliographicPhenotypesValueObject.setToHighlight( true );
                        }
                    }
                }
            }
        }
        return validateEvidenceValueObject;
    }

    /** checks to see if the evidence is already in the database */
    private boolean isEvidenceAlreadyInDatabase( EvidenceValueObject evidence ) {

        Collection<PhenotypeAssociation> phenotypeAssociations = this.associationService
                .findPhenotypeAssociationForGeneNCBI( evidence.getGeneNCBI() );

        Collection<EvidenceValueObject> evidenceValueObjects = EvidenceValueObject
                .convert2ValueObjects( phenotypeAssociations );

        // verify that the evidence is not a duplicate
        for ( EvidenceValueObject evidenceFound : evidenceValueObjects ) {
            if ( evidenceFound.equals( evidence ) ) {

                // if doing an update dont take into account the current evidence
                if ( evidence.getId() != null ) {
                    if ( evidenceFound.getId().equals( evidence.getId() ) ) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
