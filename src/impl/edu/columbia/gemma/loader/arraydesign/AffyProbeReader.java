package edu.columbia.gemma.loader.arraydesign;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.io.reader.BasicLineMapParser;
import edu.columbia.gemma.expression.designElement.CompositeSequence;
import edu.columbia.gemma.expression.designElement.Reporter;
import edu.columbia.gemma.sequence.biosequence.BioSequence;

/**
 * Reads Affymetrix Probe files.
 * <p>
 * Expected format is tabbed, NOT FASTA: <code>1494_f_at 1 325 359 1118 TCCCCATGAGTTTGGCCCGCAGAGT Antisense</code>. A
 * one-line header starting with the word "Probe" is permitted. In later versions of the format the second field is
 * omitted.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class AffyProbeReader extends BasicLineMapParser {

    protected static final Log log = LogFactory.getLog( AffyProbeReader.class );

    private int sequenceField = 3;

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.BasicLineParser#parseOneLine(java.lang.String)
     */
    public Object parseOneLine( String line ) {
        String[] sArray = line.split( "\t" );
        if ( sArray.length == 0 ) throw new IllegalArgumentException( "Line format is not valid" );

        String probeSetId = sArray[0];
        if ( probeSetId.startsWith( "Probe" ) ) return null;

        // if ( sArray.length < 5 ) throw new IOException( "File format is not valid" );

        String sequence = sArray[sequenceField];
        int locationInTarget = Integer.parseInt( sArray[sequenceField - 1] ); // unfortunately this depends on the
        String xcoord = sArray[sequenceField - 3];
        String ycoord = sArray[sequenceField - 2];
        // file.

        Reporter ap = Reporter.Factory.newInstance();
        ap.setRow( Integer.parseInt( xcoord ) );
        ap.setCol( Integer.parseInt( ycoord ) );
        ap.setStartInBioChar( locationInTarget );
        ap.setIdentifier( "Reporter:" + probeSetId + ":" + xcoord + ":" + ycoord );
        BioSequence immobChar = BioSequence.Factory.newInstance();
        immobChar.setSequence( sequence );
        immobChar.setIdentifier( "BioSequence:" + probeSetId + ":" + xcoord + ":" + ycoord ); // FIXME

        ap.setImmobilizedCharacteristic( immobChar );

        CompositeSequence newps = ( CompositeSequence ) get( probeSetId );

        if ( newps == null ) newps = CompositeSequence.Factory.newInstance();
        newps.setIdentifier( probeSetId );

        if ( newps.getReporters() == null ) newps.setReporters( new HashSet() );

        newps.getReporters().add( ap );
        return newps;

    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.BasicLineMapParser#getKey(java.lang.Object)
     */
    protected String getKey( Object newItem ) {
        assert newItem instanceof CompositeSequence;
        return ( ( CompositeSequence ) newItem ).getIdentifier();
    }

    /**
     * Set the index (starting from zero) of the field where the sequence is found. This varies between 4 and 5 in the
     * Affymetrix-provided files.
     * 
     * @param sequenceField
     */
    public void setSequenceField( int sequenceField ) {
        this.sequenceField = sequenceField;
    }

}
