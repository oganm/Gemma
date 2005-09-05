/*
 * The Gemma project
 * 
 * Copyright (c) 2005 Columbia University
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
package edu.columbia.gemma.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;

import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.util.RCommand;
import edu.columbia.gemma.expression.arrayDesign.ArrayDesign;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class AffyAnalyze extends RCommander {

    /**
     * Name of the variable where the affybatch data are stored in the R namespace.
     */
    public static final String AFFYBATCH_VARIABLE_NAME = "affybatch";

    private static Log log = LogFactory.getLog( AffyAnalyze.class.getName() );

    public AffyAnalyze() {
        super();
        rc.voidEval( "library(affy)" );
    }

    /**
     * Available normalization methods.
     */
    public enum normalizeMethod {
        CONSTANT, CONTRASTS, INVARIANTSET, LOESS, QSPLINE, QUANTILES, QUANTILES_ROBUST
    };

    public enum backgroundMethod {
        MAS, RMA, NONE
    };

    public enum pmCorrectMethod {
        MAS, PMONLY, SUBTRACTMM
    }

    public enum expressSummaryStatMethod {
        AVEDIFF, LIWONG, MAS, MEDIANPOLISH, PLAYEROUT
    }

    /**
     * Create a (minimal) AffyBatch object from a matrix. The object is retained in the R namespace.
     * 
     * @param affyBatchMatrix Rows represent probes, columns represent samples. The order of rows must be the same as in
     *        the native CEL file.
     * @param arrayDesign An arraydesign object which will be used to determine the CDF file to use, based on the array
     *        name.
     */
    public void AffyBatch( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign ) {

        if ( celMatrix == null ) throw new IllegalArgumentException( "Null matrix" );

        try {
            String matrixName = rc.assignMatrix( celMatrix );

            rc.assign( "cdfName", arrayDesign.getName() ); // Example "Mouse430_2".

            String affyBatchRCmd = AFFYBATCH_VARIABLE_NAME + "<-new(\"AffyBatch\", exprs=" + matrixName
                    + ", cdfName=cdfName )";
            rc.voidEval( affyBatchRCmd );
            rc.voidEval( "rm(" + matrixName + ")" ); // maybe saves memory...

            // String res = rc.eval( "class(" + AFFYBATCH_VARIABLE_NAME + ")" ).asString();
            // assert ( res.equals( "AffyBatch" ) );

        } catch ( RSrvException e ) {
            log.error( e, e );
            String error = rc.getLastError();
            log.error( "Last error from R was " + error );
            throw new RuntimeException( e );
        }
    }

    /**
     * Corresponds to a R method of the same name in the affy package.
     * 
     * @param name
     * @return
     */
    private String cleanCdfName( String name ) {
        if ( name == null || name.length() == 0 )
            throw new IllegalArgumentException( "invalid name (null or zero length" );
        name = bioCName( name );
        name = name.toLowerCase();
        name = name.replaceAll( "_", "" );
        name = name.replaceAll( "-", "" );
        name = name.replaceAll( " ", "" );
        return name;
    }

    /**
     * Special cases for internal bioconductor names, corresponds to the "mapCdfName". We do not replicate the behavior
     * of a bug in some versions of affy which caused the corresponding method in BioConductor to return "cdenv.example"
     * for all of the special cases.
     * 
     * @param cdfName
     * @return
     */
    private String bioCName( String cdfName ) {
        if ( cdfName.equals( "cdfenv.example" ) ) {
            return "cdfenv.example";
        } else if ( cdfName.equals( "3101_a03" ) ) {
            return "hu6800";
        } else if ( cdfName.equals( "EColiGenome" ) ) {
            return "ecoli";
        } else {
            return cdfName;
        }
    }

    /**
     * @param celMatrix
     * @param arrayDesign
     * @return
     */
    @SuppressWarnings("unchecked")
    public DenseDoubleMatrix2DNamed rma( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign ) {
        AffyBatch( celMatrix, arrayDesign );
        rc.voidEval( "v<-rma(" + AFFYBATCH_VARIABLE_NAME + ")" );
        log.info( "Done with RMA" );
        rc.voidEval( "m<-exprs(v)" );

        DenseDoubleMatrix2DNamed resultObject = rc.retrieveMatrix( "m" );

        // clean up.
        rc.voidEval( "rm(v)" );
        rc.voidEval( "rm(m)" );

        return resultObject;

    }

    /**
     * @param celMatrix
     * @param arrayDesign
     * @return
     */
    public DenseDoubleMatrix2DNamed pmAdjust( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign,
            pmCorrectMethod method ) {
        AffyBatch( celMatrix, arrayDesign );

        switch ( method ) {
        // FIXME
        }
        return null;
    }

    /**
     * @param celMatrix
     * @param arrayDesign
     * @return
     */
    public DenseDoubleMatrix2DNamed summarize( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign,
            expressSummaryStatMethod method ) {
        AffyBatch( celMatrix, arrayDesign );
        switch ( method ) {
        // FIXME
        }
        return null;
    }

    /**
     * @param celMatrix
     * @param arrayDesign
     * @return
     */
    public DenseDoubleMatrix2DNamed normalize( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign,
            normalizeMethod method ) {
        AffyBatch( celMatrix, arrayDesign );
        switch ( method ) {
        // FIXME
        }
        return null;
    }

    /**
     * Do something about background.
     * 
     * @param celMatrix
     * @param arrayDesign
     * @return
     */
    public DenseDoubleMatrix2DNamed backgroundTreat( DenseDoubleMatrix2DNamed celMatrix, ArrayDesign arrayDesign,
            backgroundMethod method ) {
        AffyBatch( celMatrix, arrayDesign );
        switch ( method ) {
        // FIXME
        }
        return null;
    }

}
