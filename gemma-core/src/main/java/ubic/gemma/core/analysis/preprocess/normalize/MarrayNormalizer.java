/*
 * The Gemma project
 *
 * Copyright (c) 2006 University of British Columbia
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
package ubic.gemma.core.analysis.preprocess.normalize;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.gemma.core.analysis.util.MArrayRaw;
import ubic.gemma.core.analysis.util.RCommander;

import java.io.IOException;

/**
 * Normalizer that uses the mArray methods from BioConductor. This is used to build specific types of preprocessors.
 *
 * @author pavlidis
 * @deprecated because we don't like to use the R integration
 */
@Deprecated
public abstract class MarrayNormalizer extends RCommander implements TwoChannelNormalizer {

    public MarrayNormalizer() throws IOException {
        super();
        boolean ok = rc.loadLibrary( "marray" );
        if ( !ok ) {
            throw new IllegalStateException( "Could not locate 'marray' library" );
        }
    }

    /**
     * Apply a normalization method from the marray BioConductor package. This method yields normalized log ratios, so
     * the summarization step is included as well.
     *
     * @param method               Name of the method (or its valid abbreviation), such as "median", "loess", "printtiploess".
     * @param channelOneBackground channel one background
     * @param channelTwoBackground channel two background
     * @param weights              weights
     * @param channelOneSignal     channel one signal
     * @param channelTwoSignal     channel two signal
     * @return the normalized double matrix
     */
    protected DoubleMatrix<String, String> normalize( DoubleMatrix<String, String> channelOneSignal,
            DoubleMatrix<String, String> channelTwoSignal, DoubleMatrix<String, String> channelOneBackground,
            DoubleMatrix<String, String> channelTwoBackground, DoubleMatrix<String, String> weights, String method ) {
        MArrayRaw mRaw = new MArrayRaw( this.rc );
        mRaw.makeMArrayLayout( channelOneSignal.rows() );
        String mRawVarName = mRaw
                .makeMArrayRaw( channelOneSignal, channelTwoSignal, channelOneBackground, channelTwoBackground,
                        weights );

        String normalizedMatrixVarName = "normalized." + channelOneSignal.hashCode();
        rc.voidEval( normalizedMatrixVarName + "<-maM(maNorm(" + mRawVarName + ", norm=\"" + method + "\" ))" );
        log.info( "Done normalizing" );

        // the normalized
        DoubleMatrix<String, String> resultObject = rc.retrieveMatrix( normalizedMatrixVarName );

        // clean up.
        rc.remove( mRawVarName );
        rc.remove( normalizedMatrixVarName );
        return resultObject;
    }

    /**
     * Apply a normalization method from the marray BioConductor package, disregarding background. This method yields
     * normalized log ratios, so the summarization step is included as well.
     *
     * @param method           Name of the method (or its valid abbreviation), such as "median", "loess", "printtiploess".
     * @param channelOneSignal channel one signal
     * @param channelTwoSignal channel two signal
     * @return the normalized double matrix
     */
    protected DoubleMatrix<String, String> normalize( DoubleMatrix<String, String> channelOneSignal,
            DoubleMatrix<String, String> channelTwoSignal, String method ) {
        MArrayRaw mRaw = new MArrayRaw( this.rc );
        mRaw.makeMArrayLayout( channelOneSignal.rows() );
        String mRawVarName = mRaw.makeMArrayRaw( channelOneSignal, channelTwoSignal, null, null, null );

        String normalizedMatrixVarName = "normalized." + channelOneSignal.hashCode();
        rc.voidEval( normalizedMatrixVarName + "<-maM(maNorm(" + mRawVarName + ", norm=\"" + method + "\" ))" );
        log.info( "Done normalizing" );

        // the normalized
        DoubleMatrix<String, String> resultObject = rc.retrieveMatrix( normalizedMatrixVarName );

        // clean up.
        rc.remove( mRawVarName );
        rc.remove( normalizedMatrixVarName );
        return resultObject;
    }

}
