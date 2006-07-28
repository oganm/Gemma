/*
 * The Gemma project
 * 
 * Copyright (c) 2006 Columbia University
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
package ubic.gemma.web.taglib.expression.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.visualization.ExpressionDataMatrix;
import ubic.gemma.visualization.ExpressionDataMatrixVisualization;

/**
 * @jsp.tag name="expressionDataMatrixVisualization" body-content="empty"
 * @author keshav
 * @version $Id$
 */
public class HtmlMatrixVisualizerTag extends TagSupport {

    private static final long serialVersionUID = 6403196597063627020L;

    private Log log = LogFactory.getLog( this.getClass() );

    // TODO if you decide to add EL support, set this and not the
    // expressionDataMatrixVisualization in the setter. A good refresher is
    // here:http://www.phptr.com/articles/article.asp?p=30946&seqNum=9&rl=1. Remember, you were having problems
    // with adding EL support to this before
    // private String expressionDataMatrixVisualizationName = null;

    private ExpressionDataMatrixVisualization expressionDataMatrixVisualization = null;

    /**
     * @jsp.attribute description="The expressionDataMatrixVisualization object" required="true" rtexprvalue="true"
     * @param expressionDataMatrixVisualization
     */
    public void setExpressionDataMatrixVisualization(
            ExpressionDataMatrixVisualization expressionDataMatrixVisualization ) {
        this.expressionDataMatrixVisualization = expressionDataMatrixVisualization;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        log.debug( "start tag" );

        // get metadata from ExpressionDataMatrixVisualization
        ExpressionDataMatrix expressionDataMatrix = expressionDataMatrixVisualization.getExpressionDataMatrix();
        String outfile = expressionDataMatrixVisualization.getOutfile();
        // TODO use to set image size ... problem is that text gets a little distorted
        int imageWidth = expressionDataMatrixVisualization.getImageWidth();
        int imageHeight = expressionDataMatrixVisualization.getImageHeight();

        Map<String, DesignElementDataVector> m = expressionDataMatrix.getDataMap();

        List<String> designElementNames = new ArrayList( m.keySet() ); // convert set to list to set the labels

        expressionDataMatrixVisualization.setRowLabels( designElementNames );
        expressionDataMatrixVisualization.createVisualization();

        expressionDataMatrixVisualization.saveImage( outfile );/* remove me when using dynamic images */

        StringBuilder buf = new StringBuilder();

        if ( expressionDataMatrix == null || m.size() == 0 ) {
            buf.append( "No data to display" );
        } else {
            ServletResponse response = pageContext.getResponse();
            log.debug( "response " + response );

            // try {/* add me when using dynamic images */

            // log.debug( "response output stream " + response.getOutputStream() );
            // String type = expressionDataMatrixVisualization.drawDynamicImage( response.getOutputStream() );
            // log.debug( "setting content type " + type );
            // response.setContentType( type );

            log.debug( "wrapping with html" );

            // TODO get the coords from the JMatrixDisplay (see xRatio and yRatio) and construct hrefs on the fly from
            // the design element name

            Map<String, Integer> rowNamesYCoords = expressionDataMatrixVisualization.getRowNameYCoords();
            for ( String rowName : rowNamesYCoords.keySet() ) {
                log.debug( rowName + " " + rowNamesYCoords.get( rowName ) );
            }

            buf
                    .append( "<img src=\"" + outfile
                            + "\" usemap=\"#visualization\" alt=\"\" style=\"border-style:none\"/>" );
            buf.append( "<map id=\"visualization\" name=\"visualization\">" );
            buf
                    .append( "<area shape=\"rect\" alt=\"\" coords=\"105,12,126,18\" href=\"http://www.google.com\" title=\"\" />" );
            buf
                    .append( "<area shape=\"rect\" alt=\"\" coords=\"106,23,127,29\" href=\"http://www.ece.queensu.ca\" title=\"\" />" );
            buf.append( "<area shape=\"default\" nohref=\"nohref\" alt=\"\" />" );
            buf.append( "</map>" );

            // } catch ( IOException e ) {
            // throw new JspException( e );
            // }
        }

        try {
            pageContext.getOut().print( buf.toString() );
        } catch ( Exception ex ) {
            throw new JspException( "HtmlMatrixVisualizationTag: " + ex.getMessage() );
        }

        log.debug( "return SKIP_BODY" );
        return SKIP_BODY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        log.debug( "end tag" );

        return EVAL_PAGE;
    }

}
