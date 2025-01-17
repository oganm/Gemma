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
package ubic.gemma.core.loader.expression.geo.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Used to contain GEO summary information from the 'Browse' views.
 *
 * @author pavlidis
 */
@SuppressWarnings("unused") // Possible external use
public class GeoRecord extends GeoData {

    private static final long serialVersionUID = 2060148205381855991L;

    private String contactName = "";
    private Collection<Long> correspondingExperiments;
    private String meshHeadings = "";
    private int numSamples;
    private Collection<String> organisms;
    private String platform = ""; // can be more than one here, for mixed data type series
    private String libraryStrategy = "";
    /*
     * How many times a curator has already looked at the details. this helps us track data sets we've already examined
     * for usefulness.
     */
    private int previousClicks = 0;
    private String pubMedIds = "";
    private Date releaseDate;
    private String sampleDetails = "";
    private Collection<String> sampleGEOAccessions;
    private String seriesType = "";
    private boolean subSeries = false;
    private String subSeriesOf = "";
    private String summary = "";
    private String overallDesign = "";
    private boolean superSeries = false;
    /*
     * Curator judgement about whether this is loadable. False indicates a problem.
     */
    private boolean usable = true;

    public GeoRecord() {
        super();
        this.organisms = new HashSet<>();
        this.correspondingExperiments = new HashSet<>();
    }

    public String getLibraryStrategy() {
        return libraryStrategy;
    }

    public void setLibraryStrategy( String libraryStrategy ) {
        this.libraryStrategy = libraryStrategy;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName( String contactName ) {
        this.contactName = contactName;
    }

    public Collection<Long> getCorrespondingExperiments() {
        return correspondingExperiments;
    }

    public void setCorrespondingExperiments( Collection<Long> correspondingExperiments ) {
        this.correspondingExperiments = correspondingExperiments;
    }

    public String getMeshHeadings() {
        return meshHeadings;
    }

    /**
     * @param meshheadings
     */
    public void setMeshHeadings( String meshheadings ) {
        this.meshHeadings = meshheadings;

    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples( int numSamples ) {
        this.numSamples = numSamples;
    }

    public Collection<String> getOrganisms() {
        return organisms;
    }

    public void setOrganisms( Collection<String> organisms ) {
        this.organisms = organisms;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform( String platform ) {
        this.platform = platform;
    }

    public int getPreviousClicks() {
        return previousClicks;
    }

    public void setPreviousClicks( int previousClicks ) {
        this.previousClicks = previousClicks;
    }

    public String getPubMedIds() {
        return this.pubMedIds;
    }

    public void setPubMedIds( String pubMedIds ) {
        if ( StringUtils.isNotBlank( pubMedIds ) )
            this.pubMedIds = pubMedIds;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate( Date releaseDate ) {
        this.releaseDate = releaseDate;
    }

    public String getSampleDetails() {
        return sampleDetails;
    }

    public void setSampleDetails( String sampleDetails ) {
        this.sampleDetails = sampleDetails;
    }

    public Collection<String> getSampleGEOAccessions() {
        return sampleGEOAccessions;
    }

    public void setSampleGEOAccessions( Collection<String> sampleGEOAccessions ) {
        this.sampleGEOAccessions = sampleGEOAccessions;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType( String seriesType ) {
        this.seriesType = seriesType;
    }

    public String getSubSeriesOf() {
        return subSeriesOf;
    }

    /**
     * @param relTo
     */
    public void setSubSeriesOf( String relTo ) {
        this.subSeriesOf = relTo;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary( String summary ) {
        this.summary = summary;
    }

    public boolean isSubSeries() {
        return subSeries;
    }

    /**
     * @param b
     */
    public void setSubSeries( boolean b ) {
        this.subSeries = b;
    }

    public boolean isSuperSeries() {
        return this.superSeries;
    }

    /**
     * @param contains
     */
    public void setSuperSeries( boolean isSuperSeries ) {
        this.superSeries = isSuperSeries;
    }

    public boolean isUsable() {
        return usable;
    }

    public void setUsable( boolean usable ) {
        this.usable = usable;
    }


    public String getOverallDesign() {
        return overallDesign;
    }

    public void setOverallDesign( String overallDesign ) {
        this.overallDesign = overallDesign;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.getTitle();
    }

}
