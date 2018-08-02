/*
 * The Gemma project.
 *
 * Copyright (c) 2006-2012 University of British Columbia
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
package ubic.gemma.model.expression.biomaterial;

@SuppressWarnings({ "unused", "WeakerAccess" }) // Possible external use
public abstract class Compound extends ubic.gemma.model.common.Describable {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8136911145519622152L;
    private String registryNumber;

    /**
     * No-arg constructor added to satisfy javabean contract
     *
     * @author Paul
     */
    public Compound() {
    }

    /**
     * @return CAS registry number (see http://www.cas.org/)
     */
    public String getRegistryNumber() {
        return this.registryNumber;
    }

    public void setRegistryNumber( String registryNumber ) {
        this.registryNumber = registryNumber;
    }

    public static final class Factory {

        public static ubic.gemma.model.expression.biomaterial.Compound newInstance() {
            return new ubic.gemma.model.expression.biomaterial.CompoundImpl();
        }

        public static ubic.gemma.model.expression.biomaterial.Compound newInstance( String name, String description,
                String registryNumber ) {
            final ubic.gemma.model.expression.biomaterial.Compound entity = new ubic.gemma.model.expression.biomaterial.CompoundImpl();
            entity.setName( name );
            entity.setDescription( description );
            entity.setRegistryNumber( registryNumber );
            return entity;
        }
    }

}