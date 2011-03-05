/*
 * The Gemma project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.gemma.loader.protein.biomart.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Value object that represents a file record line from BioMart as configured with query parameters. Which follows the
 * structure: ensembleGeneId enemblTranscriptId entrezgene ensembl_peptide_id and optionally for humans hgnc_id. There
 * is a many to many relationship between Ensembl genes ids and entrezgene. As such this value object holds a collection
 * of entrezgene strings.
 * 
 * @author ldonnison
 * @version $Id$
 */
public class Ensembl2NcbiValueObject implements Serializable {

    private static final long serialVersionUID = -859220901359582113L;

    private Integer ncbiTaxonId = 0;
    private String ensembleGeneId = "";
    private String enemblTranscriptId = "";
    private Collection<String> ncbiGenes = new HashSet<String>();

    /**
     * @return Collection of strings representing entrez genes mapping to the ensemble id.
     */
    public Collection<String> getEntrezgenes() {

        return ncbiGenes;
    }

    String ensemblPeptideId = "";
    String hgcnId = "";

    public String getHgnc_id() {
        return hgcnId;
    }

    public void setHgnc_id( String hgcnId ) {
        this.hgcnId = hgcnId;
    }

    public Integer getNcbiTaxonId() {
        return ncbiTaxonId;
    }

    public void setNcbiTaxonId( Integer ncbiTaxonId ) {
        this.ncbiTaxonId = ncbiTaxonId;
    }

    public String getEnsemblGeneId() {
        return ensembleGeneId;
    }

    public void setEnsemblGeneId( String ensembleGeneId ) {
        this.ensembleGeneId = ensembleGeneId;
    }

    public String getEnsemblTranscriptId() {
        return enemblTranscriptId;
    }

    public void setEnsemblTranscriptId( String enemblTranscriptId ) {
        this.enemblTranscriptId = enemblTranscriptId;
    }

    public String getEnsemblPeptideId() {
        return ensemblPeptideId;
    }

    public void setEnsemblPeptideId( String ensemblPeptideId ) {
        this.ensemblPeptideId = ensemblPeptideId;
    }

}
