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
package ubic.gemma.core.loader.expression.geo.fetcher;

import ubic.gemma.persistence.util.Settings;

public class SeriesFetcher extends GeoFetcher {

    @Override
    protected String formRemoteFilePath( String identifier ) {
        // ftp://ftp.ncbi.nlm.nih.gov/geo/series/GSE56nnn/GSE56785/soft/GSE56785_family.soft.gz
        // the GSE ID is changed to GSEnnn; if the GSE ID has more than three digits, then the previous digits are retained.
        String idroot = identifier.replaceFirst( "(GSE[0-9]*?)[0-9]{1,3}$", "$1nnn" );
        return remoteBaseDir + "/" + idroot + "/" + identifier + "/soft/" + identifier + "_family.soft.gz";
    }

    @Override
    protected void initConfig() {
        this.localBasePath = Settings.getString( "geo.local.datafile.basepath" );
        this.remoteBaseDir = Settings.getString( "geo.remote.seriesDir" );
    }
}
