/*
 * The Gemma project
 *
 * Copyright (c) 2021 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.core.apps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import ubic.gemma.core.annotation.reference.BibliographicReferenceService;
import ubic.gemma.core.loader.entrez.pubmed.PubMedSearch;
import ubic.gemma.core.loader.expression.geo.model.GeoRecord;
import ubic.gemma.core.loader.expression.geo.service.GeoBrowser;
import ubic.gemma.core.util.AbstractCLIContextCLI;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.ExternalDatabase;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.persistence.persister.Persister;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Identify experiments in Gemma that have no publication
 * Fetch their GEO records and check for pubmed IDs
 * Add the publications where we find them.
 */
public class UpdatePubMedCli extends AbstractCLIContextCLI {
    @Override
    public String getCommandName() {
        return "findDatasetPubs";
    }


    @Override
    public String getShortDesc() {
        return "Identify experiments that have no publication in Gemma and try to fill it in.";
    }

    @Override
    protected void buildOptions( Options options ) {

    }

    @Override
    protected void doWork() throws Exception {

        ExpressionExperimentService eeserv = this.getBean( ExpressionExperimentService.class );
        Map<String, ExpressionExperiment> toFetch = new HashMap<>();
        Collection<ExpressionExperiment> ees = eeserv.getExperimentsLackingPublications();
        for ( ExpressionExperiment ee : ees ) {
            String shortName = ee.getShortName();
            if ( shortName.contains( "." ) ) {
                ee = eeserv.thawLite( ee );
                shortName = ee.getAccession().getAccession();
            }
            toFetch.put( shortName, ee );
        }
        log.info( "Found " + toFetch.size() + " experiments lacking publications in Gemma.." );

        GeoBrowser gbs = new GeoBrowser();
        Collection<GeoRecord> geoRecords = gbs.getGeoRecords( toFetch.keySet() );

        int numFound = 0;
        for ( GeoRecord rec : geoRecords ) {
            if ( StringUtils.isBlank( rec.getPubMedIds() ) ) {
                continue;
            }
            log.info( "New PubMed(s) for " + rec.getGeoAccession() );

            ExpressionExperiment expressionExperiment = toFetch.get( rec.getGeoAccession() );

            expressionExperiment = eeserv.thawLite( expressionExperiment );

            try {
                String[] pmids = rec.getPubMedIds().split( "," );

                String pubmedId = pmids[0];

                BibliographicReference publication = getBibliographicReference( pubmedId );

                if ( publication != null ) {
                    expressionExperiment.setPrimaryPublication( publication );
                }

                if ( pmids.length > 1 ) {
                    for ( int i = 1; i < pmids.length; i++ ) {
                        publication = getBibliographicReference( pubmedId );

                        if ( publication != null ) {
                            expressionExperiment.getOtherRelevantPublications().add( publication );
                        }
                    }
                }

                eeserv.update( expressionExperiment );
                numFound++;
                addSuccessObject( expressionExperiment, "Publication(s) added" );
            } catch ( Exception e ) {
                log.error( e.getMessage() + " while processing " + rec.getGeoAccession() );
                addErrorObject( expressionExperiment, e.getMessage() );
            }


        }
        log.info( "Found publications for " + numFound + " experiments" );
    }

    @Override
    protected void processOptions( CommandLine commandLine ) throws Exception {

    }

    @Override
    public GemmaCLI.CommandGroup getCommandGroup() {
        return GemmaCLI.CommandGroup.EXPERIMENT;
    }


    /**
     * Find and persist (if necessary) the given publication.
     * @param pubmedId pubmedID
     * @return persisted reference
     */
    private BibliographicReference getBibliographicReference( String pubmedId ) {
        // check if it already in the system
        BibliographicReferenceService bibliographicReferenceService = this.getBean( BibliographicReferenceService.class );
        Persister persisterHelper = this.getPersisterHelper();
        BibliographicReference publication = bibliographicReferenceService.findByExternalId( pubmedId );
        if ( publication == null ) {
            PubMedSearch pms = new PubMedSearch();
            Collection<String> searchTerms = new ArrayList<>();
            searchTerms.add( pubmedId );
            Collection<BibliographicReference> publications;
            try {
                publications = pms.searchAndRetrieveIdByHTTP( searchTerms );
            } catch ( IOException e ) {
                throw new RuntimeException( e );
            }
            publication = publications.iterator().next();

            DatabaseEntry pubAccession = DatabaseEntry.Factory.newInstance();
            pubAccession.setAccession( pubmedId );
            ExternalDatabase ed = ExternalDatabase.Factory.newInstance();
            ed.setName( "PubMed" );
            pubAccession.setExternalDatabase( ed );

            publication.setPubAccession( pubAccession );
            publication = ( BibliographicReference ) persisterHelper.persist( publication );

        }
        return publication;
    }
}
