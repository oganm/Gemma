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
package ubic.gemma.core.image.aba;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author kelsey Represents a gene in the allen brain
 * atals
 */
@SuppressWarnings("unused") // Possible external use
public class AbaGene {

    private Integer geneId;
    private String geneSymbol;
    private String geneName;
    private Integer entrezGeneId;
    private String ncbiAccession;
    private Collection<ImageSeries> imageSeries;
    private String geneUrl;

    @SuppressWarnings({ "unused", "WeakerAccess" }) // Possible external use
    public AbaGene() {
        super();
    }

    @SuppressWarnings({ "unused", "WeakerAccess" }) // Possible external use
    public AbaGene( Integer geneId, String geneSymbol, String geneName, Integer entrezGeneId, String ncbiAccession,
            String geneUrl, Collection<ImageSeries> imageSeries ) {
        this();
        this.geneId = geneId;
        this.geneSymbol = geneSymbol;
        this.geneName = geneName;
        this.entrezGeneId = entrezGeneId;
        this.ncbiAccession = ncbiAccession;
        this.imageSeries = imageSeries;
        this.geneUrl = geneUrl;
    }

    public void addImageSeries( ImageSeries is ) {
        if ( imageSeries == null )
            imageSeries = new HashSet<>();

        imageSeries.add( is );

    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId( Integer entrezGeneId ) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getGeneId() {
        return geneId;
    }

    public void setGeneId( Integer geneId ) {
        this.geneId = geneId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName( String geneName ) {
        this.geneName = geneName;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol( String geneSymbol ) {
        this.geneSymbol = geneSymbol;
    }

    public String getGeneUrl() {
        return geneUrl;
    }

    public void setGeneUrl( String geneUrl ) {
        this.geneUrl = geneUrl;
    }

    public Collection<ImageSeries> getImageSeries() {
        return imageSeries;
    }

    public void setImageSeries( Collection<ImageSeries> imageSeries ) {
        this.imageSeries = imageSeries;
    }

    public String getNcbiAccession() {
        return ncbiAccession;
    }

    public void setNcbiAccession( String ncbiAccession ) {
        this.ncbiAccession = ncbiAccession;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "GeneId: " ).append( this.geneId ).append( "\n" );
        sb.append( "GeneSymbol: " ).append( this.geneSymbol ).append( "\n" );
        sb.append( "GeneName: " ).append( this.geneName ).append( "\n" );
        sb.append( "EntrezId: " ).append( this.entrezGeneId ).append( "\n" );
        sb.append( "NCBI Accession: " ).append( this.ncbiAccession ).append( "\n" );
        sb.append( "aba Gene Url: " ).append( this.geneUrl ).append( "\n" );

        for ( ImageSeries is : imageSeries ) {
            sb.append( "==> " );
            sb.append( "\t image series id: " ).append( is.getImageSeriesId() );
            sb.append( " \t plane: " ).append( is.getPlane() );
            sb.append( "\n" );

            if ( is.getImages() == null )
                continue;

            for ( Image img : is.getImages() ) {
                sb.append( "\t ==> " );
                sb.append( "\t \t image id: " ).append( img.getId() );
                sb.append( " \t \t Display Name: " ).append( img.getDisplayName() );
                sb.append( " \t \t Expression Image Path Url: " ).append( img.getDownloadExpressionPath() );
                sb.append( " \t \t Image path url: " ).append( img.getDownloadImagePath() );
                sb.append( " \t \t Expression thumbnail url: " ).append( img.getExpressionThumbnailUrl() );
                sb.append( " \t \t thumbnail url: " ).append( img.getThumbnailUrl() );
                sb.append( " \t \t zoomified Nissl Url: " ).append( img.getZoomifiedNisslUrl() );
                sb.append( " \t \t Posisiton: " ).append( img.getPosition() );
                sb.append( " \t \t Reference Atlas Index: " ).append( img.getReferenceAtlasIndex() );
                sb.append( "\n" );
            }

        }

        return sb.toString();
    }

}
