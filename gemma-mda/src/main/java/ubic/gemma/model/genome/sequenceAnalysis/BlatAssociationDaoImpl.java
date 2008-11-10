/*
 * The Gemma project.
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
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package ubic.gemma.model.genome.sequenceAnalysis;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.gene.GeneProduct;
import ubic.gemma.util.BusinessKey;

/**
 * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation
 */
public class BlatAssociationDaoImpl extends ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDaoBase {

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDaoBase#find(ubic.gemma.model.genome.biosequence.BioSequence
     * )
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<BlatAssociation> find( BioSequence bioSequence ) {

        BusinessKey.checkValidKey( bioSequence );

        Criteria queryObject = super.getSession( false ).createCriteria( BlatAssociation.class );

        BusinessKey.attachCriteria( queryObject, bioSequence, "bioSequence" );

        return queryObject.list();

    }

    /*
     * (non-Javadoc)
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDaoBase#find(ubic.gemma.model.genome.Gene)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<BlatAssociation> find( Gene gene ) {

        if ( gene.getProducts().size() == 0 ) {
            throw new IllegalArgumentException( "Gene has no products" );
        }

        Collection<BlatAssociation> result = new HashSet<BlatAssociation>();

        for ( GeneProduct geneProduct : gene.getProducts() ) {

            BusinessKey.checkValidKey( geneProduct );

            Criteria queryObject = super.getSession( false ).createCriteria( BlatAssociation.class );
            Criteria innerQuery = queryObject.createCriteria( "geneProduct" );

            if ( StringUtils.isNotBlank( geneProduct.getNcbiId() ) ) {
                innerQuery.add( Restrictions.eq( "ncbiId", geneProduct.getNcbiId() ) );
            }

            if ( StringUtils.isNotBlank( geneProduct.getName() ) ) {
                innerQuery.add( Restrictions.eq( "name", geneProduct.getName() ) );
            }

            result.addAll( queryObject.list() );
        }

        return result;

    }

    @Override
    protected void handleThaw( final BlatAssociation blatAssociation ) throws Exception {
        if ( blatAssociation == null ) return;
        if ( blatAssociation.getId() == null ) return;
        HibernateTemplate templ = this.getHibernateTemplate();
        templ.execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                thawBlatAssociation( session, blatAssociation );
                return null;
            }
        }, true );
    }

    @Override
    protected void handleThaw( final Collection blatAssociations ) throws Exception {
        if ( blatAssociations == null ) return;
        HibernateTemplate templ = this.getHibernateTemplate();
        templ.execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( Object object : blatAssociations ) {
                    BlatAssociation blatAssociation = ( BlatAssociation ) object;
                    if ( ( blatAssociation ).getId() == null ) continue;
                    thawBlatAssociation( session, blatAssociation );
                }

                return null;
            }

        }, true );
    }

    private void thawBlatAssociation( org.hibernate.Session session, BlatAssociation blatAssociation ) {
        session.update( blatAssociation );
        session.update( blatAssociation.getBioSequence() );
        session.update( blatAssociation.getGeneProduct() );
        session.update( blatAssociation.getGeneProduct().getGene() );
        session.update( blatAssociation.getGeneProduct().getGene().getPhysicalLocation() );
        blatAssociation.getGeneProduct().getGene().getProducts().size();
        session.update( blatAssociation.getBioSequence() );
        blatAssociation.getBioSequence().getSequenceDatabaseEntry();
    }
}