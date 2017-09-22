/*
 * The Gemma project
 * 
 * Copyright (c) 2009 University of British Columbia
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
package ubic.gemma.core.analysis.report;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubic.gemma.core.analysis.util.ExperimentalDesignUtils;
import ubic.gemma.model.analysis.expression.diff.ContrastResult;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisResult;
import ubic.gemma.model.analysis.expression.diff.ExpressionAnalysisResultSet;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.designElement.CompositeSequenceService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

/**
 * Generates textual views of the database so other people can use the data.
 * Development of this was started due to the collaboration with NIF.
 * It is essential that these views be created by a principal with Anonymous status, so as not to create views of
 * private data (that could be done, but would be separate).
 *
 * @author paul
 */
@Component
public class DatabaseViewGeneratorImpl implements DatabaseViewGenerator {

    private static final double THRESH_HOLD = 0.01;
    private static final String DATASET_SUMMARY_VIEW_BASENAME = "DatasetSummary";
    private static final String DATASET_TISSUE_VIEW_BASENAME = "DatasetTissue";
    private static final String DATASET_DIFFEX_VIEW_BASENAME = "DatasetDiffEx";
    private static Log log = LogFactory.getLog( DatabaseViewGeneratorImpl.class );
    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private CompositeSequenceService compositeSequenceService;

    @Autowired
    private DifferentialExpressionAnalysisService differentialExpressionAnalysisService;

    @Autowired
    private ArrayDesignService arrayDesignService;

    @Override
    public void runAll( Integer limit ) {
        Collection<ExpressionExperiment> ees = expressionExperimentService.loadAll();
        try {
            generateDatasetView( limit, ees );
            generateDatasetTissueView( limit, ees );
            generateDifferentialExpressionView( limit, ees );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void runAll() {
        runAll( null );
    }

    @Override
    public void generateDatasetView( int limit, Collection<ExpressionExperiment> experiments )
            throws FileNotFoundException, IOException {

        log.info( "Generating dataset summary view" );

        /*
         * Get handle to output file
         */
        File file = getViewFile( DATASET_SUMMARY_VIEW_BASENAME );
        log.info( "Writing to " + file );
        try (Writer writer = new OutputStreamWriter( new GZIPOutputStream( new FileOutputStream( file ) ) );) {

            writer.write( "GemmaDsId\tSource\tSourceAccession\tShortName\tName\tDescription\ttaxon\tManufacturer\n" );

            /*
             * Print out their names etc.
             */
            int i = 0;
            for ( ExpressionExperiment ee : experiments ) {
                ee = expressionExperimentService.thawLite( ee );
                log.info( "Processing: " + ee.getShortName() );

                String acc = "";
                String source = "";

                if ( ee.getAccession() != null && ee.getAccession().getAccession() != null ) {
                    acc = ee.getAccession().getAccession();
                    source = ee.getAccession().getExternalDatabase().getName();
                }

                Long gemmaId = ee.getId();
                String shortName = ee.getShortName();
                String name = ee.getName();
                String description = ee.getDescription();
                description = StringUtils.replaceChars( description, '\t', ' ' );
                description = StringUtils.replaceChars( description, '\n', ' ' );
                description = StringUtils.replaceChars( description, '\r', ' ' );

                Taxon taxon = expressionExperimentService.getTaxon( ee );

                if ( taxon == null )
                    continue;

                Collection<ArrayDesign> ads = expressionExperimentService.getArrayDesignsUsed( ee );
                StringBuffer manufacturers = new StringBuffer();

                // TODO could cache the arrayDesigns to make faster, thawing ad is time consuming
                for ( ArrayDesign ad : ads ) {
                    ad = arrayDesignService.thawLite( ad );
                    if ( ad.getDesignProvider() == null ) {
                        log.debug( "Array Design: " + ad.getShortName()
                                + " has no design provoider assoicated with it. Skipping" );
                        continue;
                    }
                    manufacturers.append( ad.getDesignProvider().getName() + "," );
                }

                writer.write( String.format( "%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", gemmaId, source, acc, shortName, name,
                        description, taxon.getCommonName(), StringUtils.removeEnd( manufacturers.toString(), "," ) ) );

                if ( limit > 0 && ++i > limit )
                    break;

            }

        }
    }

    @Override
    public void generateDatasetTissueView( int limit, Collection<ExpressionExperiment> experiments )
            throws FileNotFoundException, IOException {
        log.info( "Generating dataset tissue view" );

        /*
         * Get handle to output file
         */
        File file = getViewFile( DATASET_TISSUE_VIEW_BASENAME );
        log.info( "Writing to " + file );
        try (Writer writer = new OutputStreamWriter( new GZIPOutputStream( new FileOutputStream( file ) ) );) {

            /*
             * For all of their annotations... if it's a tissue, print out a line
             */
            writer.write( "GemmaDsId\tTerm\tTermURI\n" );
            int i = 0;
            for ( ExpressionExperiment ee : experiments ) {
                ee = expressionExperimentService.thawLite( ee );

                log.info( "Processing: " + ee.getShortName() );

                Long gemmaId = ee.getId();

                for ( Characteristic c : ee.getCharacteristics() ) {

                    if ( StringUtils.isBlank( c.getValue() ) ) {
                        continue;
                    }

                    /*
                     * check if vocab characteristic.
                     */

                    if ( c.getCategory().equals( "OrganismPart" ) ) { // or tissue? check URI

                        String uri = "";

                        if ( c instanceof VocabCharacteristic ) {
                            VocabCharacteristic vocabCharacteristic = ( VocabCharacteristic ) c;
                            if ( StringUtils.isNotBlank( vocabCharacteristic.getValueUri() ) )
                                uri = vocabCharacteristic.getValueUri();
                        }

                        writer.write( String.format( "%d\t%s\t%s\n", gemmaId, c.getValue(), uri ) );

                    }

                }

                if ( limit > 0 && ++i > limit )
                    break;

            }

        }
    }

    @Override
    public void generateDifferentialExpressionView( int limit, Collection<ExpressionExperiment> experiments )
            throws FileNotFoundException, IOException {
        log.info( "Generating dataset diffex view" );

        /*
         * Get handle to output file
         */
        File file = getViewFile( DATASET_DIFFEX_VIEW_BASENAME );
        log.info( "Writing to " + file );
        try (Writer writer = new OutputStreamWriter( new GZIPOutputStream( new FileOutputStream( file ) ) );) {

            /*
             * For each gene that is differentially expressed, print out a line per contrast
             */
            writer.write(
                    "GemmaDsId\tEEShortName\tGeneNCBIId\tGemmaGeneId\tFactor\tFactorURI\tBaseline\tContrasting\tDirection\n" );
            int i = 0;
            for ( ExpressionExperiment ee : experiments ) {
                ee = expressionExperimentService.thawLite( ee );

                Collection<DifferentialExpressionAnalysis> results = differentialExpressionAnalysisService
                        .getAnalyses( ee );
                if ( results == null || results.isEmpty() ) {
                    log.warn( "No differential expression results found for " + ee );
                    continue;
                }

                if ( results.size() > 1 ) {
                    /*
                     * FIXME. Should probably skip for this purpose.
                     */
                }

                log.info( "Processing: " + ee.getShortName() );

                for ( DifferentialExpressionAnalysis analysis : results ) {

                    analysis = this.differentialExpressionAnalysisService.thawFully( analysis );

                    for ( ExpressionAnalysisResultSet ears : analysis.getResultSets() ) {

                        // ears = differentialExpressionResultService.thaw( ears );

                        FactorValue baselineGroup = ears.getBaselineGroup();

                        if ( baselineGroup == null ) {
                            // log.warn( "No baseline defined for " + ee ); // interaction
                            continue;
                        }

                        if ( ExperimentalDesignUtils.isBatch( baselineGroup.getExperimentalFactor() ) ) {
                            continue;
                        }

                        String baselineDescription = ExperimentalDesignUtils.prettyString( baselineGroup );

                        // Get the factor category name
                        String factorName = "";
                        String factorURI = "";

                        for ( ExperimentalFactor ef : ears.getExperimentalFactors() ) {
                            factorName += ef.getName() + ",";
                            if ( ef.getCategory() instanceof VocabCharacteristic ) {
                                factorURI += ( ( VocabCharacteristic ) ef.getCategory() ).getCategoryUri() + ",";
                            }
                        }
                        factorName = StringUtils.removeEnd( factorName, "," );
                        factorURI = StringUtils.removeEnd( factorURI, "," );

                        if ( ears.getResults() == null || ears.getResults().isEmpty() ) {
                            log.warn( "No  differential expression analysis results found for " + ee );
                            continue;
                        }

                        // Generate probe details
                        for ( DifferentialExpressionAnalysisResult dear : ears.getResults() ) {

                            if ( dear == null ) {
                                log.warn( "Missing results for " + ee + " skipping to next. " );
                                continue;
                            }

                            if ( dear.getCorrectedPvalue() == null || dear.getCorrectedPvalue() > THRESH_HOLD )
                                continue;

                            String formatted = formatDiffExResult( ee, dear, factorName, factorURI,
                                    baselineDescription );

                            if ( StringUtils.isNotBlank( formatted ) )
                                writer.write( formatted );

                        } // dear loop
                    } // ears loop
                } // analysis loop

                if ( limit > 0 && ++i > limit )
                    break;

            } // EE loop
        }
    }

    private String formatDiffExResult( ExpressionExperiment ee,
            DifferentialExpressionAnalysisResult probeAnalysisResult, String factorName, String factorURI,
            String baselineDescription ) {

        CompositeSequence cs = probeAnalysisResult.getProbe();

        Collection<Gene> genes = compositeSequenceService.getGenes( cs );

        if ( genes.isEmpty() || genes.size() > 1 ) {
            return null;
        }

        Gene g = genes.iterator().next();

        if ( g.getNcbiGeneId() == null )
            return null;

        Collection<ContrastResult> contrasts = probeAnalysisResult.getContrasts();

        StringBuilder buf = new StringBuilder();
        for ( ContrastResult cr : contrasts ) {
            FactorValue factorValue = cr.getFactorValue();

            String direction = cr.getLogFoldChange() < 0 ? "-" : "+";

            String factorValueDescription = ExperimentalDesignUtils.prettyString( factorValue );

            buf.append( String.format( "%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\n", ee.getId(), ee.getShortName(),
                    g.getNcbiGeneId().toString(), g.getId(), factorName, factorURI, baselineDescription,
                    factorValueDescription, direction ) );
        }

        return buf.toString();
    }

    private File getViewFile( String datasetDiffexViewBasename ) {
        return getOutputFile( datasetDiffexViewBasename + VIEW_FILE_SUFFIX );
    }

    @Override
    public File getOutputFile( String filename ) {
        String fullFilePath = VIEW_DIR + filename;
        File f = new File( fullFilePath );

        if ( f.exists() ) {
            return f;
        }

        File parentDir = f.getParentFile();
        if ( !parentDir.exists() )
            parentDir.mkdirs();
        return f;
    }

}
