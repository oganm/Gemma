/*
 * The Gemma project
 * 
 * Copyright (c) 2008 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */
Ext.namespace( 'Gemma' );
Gemma.MAX_LABEL_LENGTH_CHAR = 25;
Gemma.MAX_GENEINFO_LENGTH_CHAR = 125;

/**
 * Simple display of vectors in a heatmap display, without thumbnails. Configuration : title, readMethod; then call
 * 'show' passing in the array of parameters for the read method.
 * 
 * @version $Id$
 * @deprecated
 * @author Paul based on klcs classes.
 */
Gemma.VectorDisplay = Ext.extend( Ext.Window,

{
   width : 420,
   height : 400,
   bodyStyle : "background:white",
   stateful : false,
   noGeneLabel : "[No gene]",

   plugins : [ new Ext.ux.plugins.ContainerMask( {
      msg : 'Loading ... <img src="/Gemma/images/loading.gif" />',
      masked : true
   } ) ],

   graphConfig : {
      label : true,
      forceFit : true
   },

   /**
    * @memberOf Gemma.VectorDisplay
    */
   show : function( config ) {

      this.showMask();

      var params = [];
      if ( config.params ) {
         params = config.params;
      }

      this.dataView.getStore().load( {
         params : params,
         callback : this.dedvCallback.createDelegate( this )
      } );

      Gemma.VectorDisplay.superclass.show.call( this );

   },

   dedvCallback : function( data ) {

      /*
       * Note that this only handles data from a single experiment --- data.length == 1.
       */
      if ( !data || data.length === 0 || data[0].data.profiles.length === 0 ) {
         Ext.Msg.alert( 'Status', 'No data available', function() {
            this.close();
         }, this );
         return;
      }

      this.sampleLabels = data[0].data.sampleNames;

      this.preparedData = Gemma.prepareProfiles( data[0].data, true );

      Heatmap.draw( $( this.body.id ), this.preparedData.profiles, this.graphConfig, this.sampleLabels );

      this.hideMask();

      this.doLayout();

   },

   /**
    * 
    */
   refresh : function() {
      $( this.body.id ).innerHTML = '';
      Heatmap.draw( $( this.body.id ), this.preparedData.profiles, this.graphConfig, this.sampleLabels );
   },

   initComponent : function() {

      this.dataView = new Ext.DataView( {
         autoHeight : true,
         emptyText : 'Data not available',
         loadingText : 'Loading...',
         store : new Gemma.VisualizationStore( {
            readMethod : this.readMethod
         } )
      } );

      Gemma.VectorDisplay.superclass.initComponent.call( this );

      this.on( 'resize', function( component, width, height ) {

         if ( !this.preparedData ) {
            return;
         }

         this.refresh();

      }.createDelegate( this ) );

   }

} );
