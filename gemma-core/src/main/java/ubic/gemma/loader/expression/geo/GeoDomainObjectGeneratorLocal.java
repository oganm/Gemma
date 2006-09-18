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
package ubic.gemma.loader.expression.geo;

import ubic.gemma.loader.expression.geo.fetcher.LocalDatasetFetcher;
import ubic.gemma.loader.expression.geo.fetcher.LocalSeriesFetcher;

/**
 * GEO object generator that works on local files. Created partly to assist in testing.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeoDomainObjectGeneratorLocal extends GeoDomainObjectGenerator {

    private String fileLocation;

    public GeoDomainObjectGeneratorLocal( String fileLocation ) {
        this.fileLocation = fileLocation;
        this.intialize();
    }

    @Override
    public void intialize() {
        datasetFetcher = new LocalDatasetFetcher( fileLocation );
        seriesFetcher = new LocalSeriesFetcher( fileLocation );
        platformFetcher = new LocalSeriesFetcher( fileLocation );

        this.parser = new GeoFamilyParser();
    }

}
