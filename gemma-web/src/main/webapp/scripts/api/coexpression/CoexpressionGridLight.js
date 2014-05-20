/*
 * The Gemma project
 * 
 * Copyright (c) 2013 University of British Columbia
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

/**
 * FIXME get rid of this - just make it a subclass or superclass of the CoexpressionGrid
 */
Gemma.CoexpressionGridLight = Ext.extend( Ext.grid.GridPanel, {

   collapsible : false,
   editable : false,
   style : "margin-bottom: 1em;",
   height : 300,
   autoScroll : true,
   stateful : false,

   viewConfig : {
      forceFit : true,
      emptyText : 'No coexpression to display'
   },

   /**
    * @memberOf Gemma.CoexpressionGridLight
    */
   initComponent : function() {
      this.ds = new Ext.data.Store( {
         proxy : new Ext.data.MemoryProxy( [] ),
         reader : new Ext.data.ListRangeReader( {
            id : "id"
         }, Gemma.CoexpressionGridRecordConstructor ),
         sortInfo : {
            field : 'posSupp',
            direction : 'DESC'
         }
      } );

      Ext.apply( this, {
         columns : [ {
            id : 'visualize',
            header : "Visualize",
            dataIndex : "visualize",
            renderer : this.visStyler,
            tooltip : "Link for visualizing raw data",
            sortable : false,
            width : 35
         }, {
            id : 'found',
            header : "Coexpressed Gene",
            dataIndex : "foundGene",
            renderer : this.foundGeneStyler.createDelegate( this ),
            tooltip : "Coexpressed Gene",
            sortable : true
         }, {
            id : 'support',
            header : "Support",
            dataIndex : "supportKey",
            width : 70,
            renderer : this.supportStyler.createDelegate( this ),
            tooltip : Gemma.HelpText.WidgetDefaults.CoexpressionGrid.supportColumnTT,
            sortable : true
         }, {
            id : 'gene2GeneProteinAssociationStringUrl',
            header : "PPI",
            dataIndex : "gene2GeneProteinAssociationStringUrl",
            width : 30,
            renderer : this.proteinLinkStyler.createDelegate( this ),
            tooltip : "Evidence for interactions from external sources",
            sortable : true,
            hidden : true
         }, {
            id : 'nodeDegree',
            header : "Specificity",
            dataIndex : "foundGeneNodeDegree",
            width : 60,
            renderer : this.nodeDegreeStyler.createDelegate( this ),
            tooltip : "How many links these genes have at this stringency (query, found)",
            sortable : true
         } ]
      } );

      Gemma.CoexpressionGridLight.superclass.initComponent.call( this );

      this.on( "cellclick", this.rowClickHandler, this );
   },

   /**
    * Private; CoexpressionMetaValueObject FIXME same as non-light
    * 
    * @param numQueryGenes
    * @param data
    */
   loadData : function( data ) {
      this.getStore().proxy.data = data;
      this.getStore().reload( {
         resetPage : true
      } );

   },

   /**
    * Private; Load the data; if there is no data returned an errorState message is set on the result to indicate what
    * the exact problem was. CoexpressionMetaValueObjecct. FIXME same as non-light
    * 
    * @param result
    */
   loadDataCb : function( result ) {
      if ( result.errorState ) {
         this.handleError( result.errorState );
      } else {
         this.loadData( result.results );
      }
   },

   doSearch : function( csc ) {
      this.loadMask = new Ext.LoadMask( this.getEl(), {
         msg : "Loading ..."
      } );
      this.loadMask.show();
      var errorHandler = this.handleError.createDelegate( this );
      CoexpressionSearchController.doSearch( csc, {
         callback : this.loadDataCb.createDelegate( this ),
         errorHandler : errorHandler
      } );
   },

   /**
    * Checks if store contains any results if not print message indicating that there are non. Stop loader. Called when
    * an error thrown of after data load processing
    */
   handleError : function( errorMessage ) {
      if ( Ext.get( 'coexpression-msg' ) ) {
         Ext.DomHelper.applyStyles( "coexpression-msg", "height: 2.2em" );
         Ext.DomHelper.overwrite( "coexpression-msg", [ {
            tag : 'img',
            src : '/Gemma/images/icons/information.png'
         }, {
            tag : 'span',
            html : "&nbsp;&nbsp;" + errorMessage
         } ] );
      } else {
         Ext.Msg.alert( "Warning", errorMessage );
         this.getView().refresh(); // show empty text
      }
      this.loadMask.hide();
   },

   clearError : function() {
      Ext.DomHelper.overwrite( "coexpression-messages", "" );
   },

   filter : function() {
      var text = Ext.getCmp( this.id + '-search-in-grid' ).getValue();
      var value = '';

      if ( text && text.length > 1 ) {
         value = new RegExp( Ext.escapeRe( text ), 'i' );
      }

      return function( row ) {
         var foundGene = row.get( "foundGene" );
         var queryGene = row.get( "queryGene" );

         if ( !value ) {
            return true;
         } else {
            if ( value.test( foundGene.officialSymbol ) || value.test( queryGene.officialSymbol )
               || value.test( foundGene.officialName ) || value.test( queryGene.officialName ) ) {
               return true;
            }
         }
         return false;
      };
   },

   // link for protein interactions
   proteinLinkStyler : function( value, metadata, record, row, col, ds ) {
      var data = record.data;
      var result = "";

      if ( data['gene2GeneProteinAssociationStringUrl'] ) {
         result = String.format( '<span>' + '<a href="{0}"  target="_blank" class="external">'
            + '<img src="/Gemma/images/logo/string_logo.gif" '
            + 'ext:qtip="Click to view the protein protein interaction obtained from {1} '
            + 'evidence with a combined association score of {2} from STRING" />' + '</a>' + '</span>',
            data['gene2GeneProteinAssociationStringUrl'], data['gene2GeneProteinInteractionEvidence'],
            data['gene2GeneProteinInteractionConfidenceScore'] );
      }
      if ( data['queryRegulatesFound'] ) {
         result = result + " " + '<span> <img height="16" width = "16" src="/Gemma/images/logo/pazar-icon.png"'
            + ' ext:qtip="Query may regulate the coexpressed gene, according to Pazar" />' + '</span>';
      } else if ( data['foundRegulatesQuery'] ) {
         result = result + " " + '<span> <img height="16" width = "16" src="/Gemma/images/logo/pazar-icon.png"'
            + ' ext:qtip="The query may be regulated by the coexpressed gene, according to Pazar" />' + '</span>';
      }
      return result;
   },

   /**
    * 
    * @param value
    * @param metadata
    * @param {Ext.data.Record}
    *           record
    * @param row
    * @param col
    * @param ds
    * @return {*}
    */
   nodeDegreeStyler : function( value, metadata, record, row, col, ds ) {
      var data = record.data;

      // var displayedNodeDegree = 0;
      //
      // if ( data['foundGeneNodeDegree'] === null ) {
      // return 0;
      // } else if ( data['queryGeneNodeDegree'] > data['foundGeneNodeDegree'] ) {
      // displayedNodeDegree = data['queryGeneNodeDegree'];
      // } else {
      // displayedNodeDegree = data['foundGeneNodeDegree'];
      // }

      return "<b>" + data['queryGeneNodeDegree'] + "</b>," + data['foundGeneNodeDegree'];
      // return displayedNodeDegree;
      // return Gemma.CytoscapePanelUtil.nodeDegreeBinMapper(displayedNodeDegree);
   },

   /**
    * 
    */
   supportStyler : function( value, metadata, record, row, col, ds ) {
      var data = record.data;
      if ( data['posSupp'] || data['negSupp'] ) {
         var style = "";
         if ( data['posSupp'] ) {
            style = style
               + String.format( "<span class='positiveLink'>{0}{1}</span> ", data['posSupp'], this
                  .getSpecificLinkString( data['posSupp'], data['nonSpecPosSupp'] ) );
         }
         if ( data['negSupp'] ) {
            style = style
               + String.format( "<span class='negativeLink'>{0}{1}</span> ", data['negSupp'], this
                  .getSpecificLinkString( data['negSupp'], data['nonSpecNegSupp'] ) );
         }

         if ( data['numTestedIn'] ) {
            style = style + String.format( "/ {0}", data['numTestedIn'] );
         }
         return style;
      } else {
         return "-";
      }
   },

   /**
    * For displaying Gene ontology similarity
    */
   goStyler : function( value, metadata, record, row, col, ds ) {
      var data = record.data;
      if ( data['goSim'] || data['maxGoSim'] ) {
         return String.format( "{0}/{1}", data['goSim'], data['maxGoSim'] );
      } else {
         return "-";
      }
   },

   getSpecificLinkString : function( total, nonSpecific ) {
      return nonSpecific ? String.format( "<span class='specificLink'> ({0})</span>", total - nonSpecific ) : "";
   },

   /**
    * Display the target (found) genes.
    */
   foundGeneStyler : function( value, metadata, record, row, col, ds ) {
      var gene = record.data.foundGene;

      if ( gene.officialName === null ) {
         gene.officialName = "";
      }

      if ( gene.taxonId !== null ) {
         gene.taxonId = gene.taxonId;
         gene.taxonName = gene.taxonCommonName;
      } else {
         gene.taxonId = -1;
         gene.taxonName = "?";
      }
      return this.foundGeneTemplateNoGemma.apply( gene );
   },

   /**
    * FIXME this should use the same analysis as the last query. Here we always use 'All'.
    */
   foundGeneTemplate : new Ext.Template(
      "<a href='/Gemma/searchCoexpression.html?g={id}&s=3&t={taxonId}&an=All {taxonName}'>"
         + " <img src='/Gemma/images/logo/gemmaTiny.gif' ext:qtip='Make {officialSymbol} the query gene' /> </a>",
      " &nbsp; ", "<a target='_blank' href='/Gemma/gene/showGene.html?id={id}'>{officialSymbol}</a> {officialName}" ),

   foundGeneTemplateNoGemma : new Ext.Template( "<a style='font-weight:{fontWeight};' "
      + "target='_blank' href='/Gemma/gene/showGene.html?id={id}'>{officialSymbol}</a> {officialName}" ),

   visStyler : function( value, metadata, record, row, col, ds ) {
      return "<img src='/Gemma/images/icons/chart_curve.png' ext:qtip='Visualize the data' />";
   },

   /**
    * 
    * @param grid
    * @param rowIndex
    * @param columnIndex
    */
   rowClickHandler : function( grid, rowIndex, columnIndex ) {
      if ( this.getSelectionModel().hasSelection() ) {

         var record = this.getStore().getAt( rowIndex );
         var fieldName = this.getColumnModel().getDataIndex( columnIndex );
         var queryGene = record.get( "queryGene" );
         var foundGene = record.get( "foundGene" );

         if ( fieldName === 'visualize' ) {
            var activeExperiments = record.data['supportingExperiments'];

            if ( activeExperiments === null || activeExperiments.length == 0 ) {
               Ext.Msg.alert( "Unavailable", "Details about the experiments are not available for visualization" );
               return;
            }

            var coexpressionVisualizationWindow = new Gemma.CoexpressionVisualizationWindow( {
               cascadeOnFirstShow : true,
               admin : false,
               experiments : activeExperiments,
               queryGene : queryGene,
               foundGene : foundGene,
               downloadLink : String.format( "/Gemma/dedv/downloadDEDV.html?ee={0}&g={1},{2}", activeExperiments
                  .join( ',' ), queryGene.id, foundGene.id ),
               title : "Coexpression for:  " + queryGene.name + " + " + foundGene.name
            } );

            var params = [];
            params.push( activeExperiments );
            params.push( queryGene.id );
            params.push( foundGene.id );

            coexpressionVisualizationWindow.show( {
               params : params
            } );
         }
      }
   }
} );
