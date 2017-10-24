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
package ubic.gemma.core.datastructure.matrix;

import org.apache.commons.lang3.StringUtils;
import ubic.basecode.util.DateUtil;
import ubic.gemma.core.analysis.service.ExpressionDataFileService;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.persistence.util.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author keshav
 */
@SuppressWarnings("WeakerAccess") // Possible external use
public class ExpressionDataWriterUtils {

    public static final String DELIMITER_BETWEEN_BIOMATERIAL_AND_BIOASSAYS = "___";

    /**
     * Appends base header information (about the experiment) to a file.
     *
     * @param buf         buffer
     * @param experiment  ee
     * @param fileTypeStr file type str
     */
    public static void appendBaseHeader( ExpressionExperiment experiment, String fileTypeStr, StringBuffer buf ) {

        buf.append( "# " ).append( fileTypeStr ).append( " file generated by Gemma on " )
                .append( DateUtil.convertDateToString( new Date() ) ).append( "\n" );
        if ( experiment != null ) {
            buf.append( "# shortName=" ).append( experiment.getShortName() ).append( "\n" );
            buf.append( "# name=" ).append( experiment.getName() ).append( "\n" );
            buf.append( "# Experiment details: " + Settings.getBaseUrl() + "expressionExperiment/showExpressionExperiment.html?id=" )
                    .append( experiment.getId() ).append( "\n" );
        }

        buf.append( ExpressionDataFileService.DISCLAIMER );

    }

    /**
     * Appends base header information (about the experiment) to a file.
     *
     * @param buf        buffer
     * @param design     design
     * @param experiment ee
     */
    public static void appendBaseHeader( ExpressionExperiment experiment, boolean design, StringBuffer buf ) {
        String fileType = "data";

        if ( design )
            fileType = "design";

        appendBaseHeader( experiment, "Expression " + fileType, buf );
    }

    public static String constructBioAssayName( BioMaterial bioMaterial, Collection<BioAssay> bioAssays ) {
        String colBuf = ( bioMaterial.getName() + DELIMITER_BETWEEN_BIOMATERIAL_AND_BIOASSAYS ) + StringUtils
                .join( bioAssays, "." );

        String colName = StringUtils.deleteWhitespace( colBuf );

        return constructRCompatibleBioAssayName( colName );
    }

    /**
     * Constructs a bioassay name. This is useful when writing out data to a file.
     *
     * @param matrix           matrix
     * @param assayColumnIndex The column index in the matrix.
     * @return BA name
     */
    public static String constructBioAssayName( ExpressionDataMatrix<?> matrix, int assayColumnIndex ) {

        BioMaterial bioMaterialForColumn = matrix.getBioMaterialForColumn( assayColumnIndex );
        Collection<BioAssay> bioAssaysForColumn = matrix.getBioAssaysForColumn( assayColumnIndex );

        return constructBioAssayName( bioMaterialForColumn, bioAssaysForColumn );

    }

    /**
     * Replaces spaces and hyphens with underscores.
     *
     * @param factorValue FV
     * @return replaced string
     */
    public static String constructFactorValueName( FactorValue factorValue ) {

        StringBuilder buf = new StringBuilder();

        if ( factorValue.getCharacteristics().size() > 0 ) {
            for ( Characteristic c : factorValue.getCharacteristics() ) {
                buf.append( StringUtils.strip( c.getValue() ) );
                if ( factorValue.getCharacteristics().size() > 1 )
                    buf.append( " | " );
            }
        } else if ( factorValue.getMeasurement() != null ) {
            buf.append( factorValue.getMeasurement().getValue() );
        } else if ( StringUtils.isNotBlank( factorValue.getValue() ) ) {
            buf.append( StringUtils.strip( factorValue.getValue() ) );
        }

        String matchedFactorValue = buf.toString();

        matchedFactorValue = matchedFactorValue.trim();
        matchedFactorValue = matchedFactorValue.replaceAll( "-", "_" );
        matchedFactorValue = matchedFactorValue.replaceAll( "\\s", "_" );
        return matchedFactorValue;
    }

    /**
     * @param bioAssays   BAs
     * @param bioMaterial BM
     * @return String representing the external identifier of the biomaterial. This will usually be a GEO or ArrayExpression
     * accession id, or else blank.
     */
    public static String getExternalId( BioMaterial bioMaterial, Collection<BioAssay> bioAssays ) {
        String name = "";

        if ( bioMaterial.getExternalAccession() != null ) {
            name = bioMaterial.getExternalAccession().getAccession();
        } else if ( StringUtils.isBlank( name ) && !bioAssays.isEmpty() ) {
            List<String> ids = new ArrayList<>();
            for ( BioAssay ba : bioAssays ) {
                if ( ba.getAccession() != null ) {
                    ids.add( ba.getAccession().getAccession() );
                }
            }

            name = StringUtils.join( ids, "/" );
        }

        name = StringUtils.isBlank( name ) ? "" : name;

        return constructRCompatibleBioAssayName( name );
    }

    private static String constructRCompatibleBioAssayName( String colName ) {
        String colNameMod = colName;
        colNameMod = StringUtils.replaceChars( colNameMod, ':', '.' );
        colNameMod = StringUtils.replaceChars( colNameMod, '|', '.' );
        colNameMod = StringUtils.replaceChars( colNameMod, '-', '.' );
        return colNameMod;
    }

}
