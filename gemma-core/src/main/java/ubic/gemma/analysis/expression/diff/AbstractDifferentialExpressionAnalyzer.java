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
package ubic.gemma.analysis.expression.diff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.gemma.analysis.preprocess.ExpressionDataMatrixBuilder;
import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * An abstract differential expression analyzer to be extended by analyzers
 * which will make use of R. For example, see {@link OneWayAnovaAnalyzer}.
 * 
 * @author keshav
 * @version $Id: AbstractDifferentialExpressionAnalyzer.java,v 1.14 2008/11/08
 *          22:04:20 paul Exp $
 */
public abstract class AbstractDifferentialExpressionAnalyzer extends
		AbstractAnalyzer {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * Peform an analysis where the factors are determined (or guessed)
	 * automatically. If this cannot be unambiguously determined, an exception
	 * will be thrown.
	 * 
	 * @param expressionExperiment
	 * @return ExpressionAnalysis
	 */
	public abstract DifferentialExpressionAnalysis run(
			ExpressionExperiment expressionExperiment);

	/**
	 * Perform an analysis using the specified factor(s)
	 * 
	 * @param expressionExperiment
	 * @param factors
	 * @return
	 */
	public abstract DifferentialExpressionAnalysis run(
			ExpressionExperiment expressionExperiment,
			Collection<ExperimentalFactor> factors);

	/**
	 * @param pvalues
	 * @return returns the qvalues (false discovery rates) for the pvalues using
	 *         the method of Storey and Tibshirani.
	 */
	protected double[] getQValues(double[] pvalues) {

		if (pvalues == null || pvalues.length == 0) {
			throw new IllegalArgumentException("No pvalues provided");
		}

		if (rc == null) {
			connectToR();
		}
		boolean hasQValue = rc.loadLibrary("qvalue");
		if (!hasQValue) {
			throw new IllegalStateException(
					"qvalue does not seem to be available");
		}

		StringBuffer qvalueCommand = new StringBuffer();
		String pvalsName = "pvals_" + RandomStringUtils.randomAlphabetic(10);
		rc.assign(pvalsName, pvalues);
		qvalueCommand.append("qvalue(" + pvalsName + ")$qvalues");
		double[] qvalues = rc.doubleArrayEval(qvalueCommand.toString());

		if (qvalues == null) {
			throw new IllegalStateException("Null qvalues.  Check the R side.");
		}

		if (qvalues.length != pvalues.length) {
			throw new IllegalStateException(
					"Number of q values and p values must match.  Qvalues - "
							+ qvalues.length + ": Pvalues - " + pvalues.length);
		}

		return qvalues;
	}

	/**
	 * @param pvalues
	 * @param expressionExperiment
	 * @param effects
	 *            ordered (for 2 way anova)
	 */
	protected void writePValuesHistogram(double[] pvalues,
			ExpressionExperiment expressionExperiment,
			ArrayList<ExperimentalFactor> effects) {

		File dir = DifferentialExpressionFileUtils
				.getBaseDifferentialDirectory(expressionExperiment
						.getShortName());

		FileTools.createDir(dir.toString());

		String histFileName = expressionExperiment.getShortName()
				+ DifferentialExpressionFileUtils.PVALUE_DIST_SUFFIX;

		Collection<Histogram> hists = generateHistograms(histFileName, effects,
				100, 0, 1, pvalues);

		if (hists == null || hists.isEmpty()) {
			log.error("Could not generate histogram.  Not writing to file");
			return;
		}

		for (Histogram hist : hists) {
			String path = dir + File.separator + hist.getName();

			File outputFile = new File(path); 
			try {
				FileWriter out = new FileWriter(outputFile, false); // false = clobber.
				out.write("# Differential Expression distribution\n");
				out.write("# date=" + (new Date()) + "\n");
				out.write("# exp=" + expressionExperiment + " "
						+ expressionExperiment.getShortName() + "\n");
				out.write("Bin\tCount\n");
				hist.writeToFile(out);
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param histFileName
	 * @param effects
	 *            ordered
	 * @param numBins
	 * @param min
	 * @param max
	 * @param pvalues
	 * @return
	 */
	protected abstract Collection<Histogram> generateHistograms(
			String histFileName, ArrayList<ExperimentalFactor> effects,
			int numBins, int min, int max, double[] pvalues);

	/**
	 * Returns the preferred {@link QuantitationType}.
	 * 
	 * @param vectors
	 * @return
	 */
	protected QuantitationType getPreferredQuantitationType(
			Collection<DesignElementDataVector> vectors) {
		// FIXME could be slow?
		QuantitationType qt = null;
		for (DesignElementDataVector vector : vectors) {
			qt = vector.getQuantitationType();
			if (qt.getIsPreferred()) {
				return qt;
			}
		}
		log
				.error("Could not determine the preferred quantitation type.  Not sure what type to associate with the analysis result.");
		return null;
	}

	/**
	 * Creates the matrix using the vectors. Masks the data for two color
	 * arrays.
	 * 
	 * @param vectorsToUse
	 * @return
	 */
	protected ExpressionDataDoubleMatrix createMaskedMatrix(
			Collection<DesignElementDataVector> vectorsToUse) {

		ExpressionDataMatrixBuilder builder = new ExpressionDataMatrixBuilder(
				vectorsToUse);

		ExpressionDataDoubleMatrix dmatrix = builder.getProcessedData();

		return dmatrix;
	}
}
