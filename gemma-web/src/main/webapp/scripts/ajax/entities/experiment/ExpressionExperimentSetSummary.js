Ext.namespace('Gemma');
Ext.BLANK_IMAGE_URL = '/Gemma/images/default/s.gif';
/**
 *
 * Panel containing the most interesting info about an experiment.
 * Used as one tab of the EE page
 *
 * pass in the ee details obj as experimentDetails
 *
 * @class Gemma.ExpressionExperimentDetails
 * @extends Ext.Panel
 *
 */
Gemma.ExpressionExperimentSetSummary = Ext.extend(Ext.Panel, {
	
	layout:'vbox',
	layoutConfig:{
		align:'stretch'
	},
	dirtyForm : false,
	listeners:{
		leavingTab: function(){
			if(this.editModeOn && this.dirtyForm){
				var leave = confirm("You are still in edit mode. Your unsaved changes will be discarded when you switch tabs. Do you want to continue?");
				if(leave){
					return true;
				}
				return false;
			}
			return true;
		},
		tabChanged: function(){
			this.fireEvent('toggleEditMode', false);
		}
	},
	
    renderStatus: function(e){
		var statusString = "";
		if (!e.modifiable) {
			statusString += "<img src='/Gemma/images/icons/shield.png' height='16' width='16' "+
				"title='Protected; cannot have members changed, usually applies to automatically generated groups.' />&nbsp;"
		}
		var sl = Gemma.SecurityManager.getSecurityLink("ubic.gemma.model.analysis.expression.ExpressionExperimentSetImpl", 
					e.id, e.publik, e.shared, e.currentUserHasWritePermission);
					
		statusString += sl;
		return statusString;
	},
    renderNumber: function(e){
		var numString = "" + e.numExperiments;
		return numString;
	},
    

    initComponent: function(){
    
        Gemma.ExpressionExperimentSetSummary.superclass.initComponent.call(this);

		
        var e = this.experimentSet;
        
        // if no permissions hasWritePermission is not set.
        if ( e.currentUserHasWritePermission && e.modifiable) {
            this.editable = true;
        }

        var currentDescription = e.description;
        var currentName = e.name;
        
        save = function(){
			if (!this.saveMask) {
				this.saveMask = new Ext.LoadMask(this.getEl(), {
					msg: "Saving ..."
				});
			}
			this.saveMask.show();
            var description = descriptionArea.getValue();
            var name = nameArea.getValue();
            
            var entity = {
                id: e.id,
				description : description,
                name : name
            };

			// returns ee details object
            ExpressionExperimentSetController.updateNameDesc(entity, function(data){
			
	                nameArea.setValue(data.name);
	                descriptionArea.setValue(data.description);
	                
	                currentName = data.name;
	                currentDescription = data.description;
					
					this.dirtyForm = false;
					this.saveMask.hide();
				
            }.createDelegate(this));
			
        }.createDelegate(this);
        
        var descriptionArea = new Ext.form.TextArea({
            allowBlank: true,
            resizable: true,
            readOnly: true,
            disabled: true,
			growMin:1,
			growMax:150,
			growAppend:'',
			grow : true,
            disabledClass: 'disabled-plain',
            fieldClass: '',
            emptyText: 'No description provided',
            enableKeyEvents: true,
            bubbleEvents: ['changeMade'],
            listeners: {
                'keyup': function(field, e){
                    if (field.isDirty()) {
                        field.fireEvent('changeMade', field.isValid());
                    }
                },
                'toggleEditMode': function(editOn){
                    this.setReadOnly(!editOn);
                    this.setDisabled(!editOn);
                    if (editOn) {
                        this.removeClass('x-bare-field');
                    }
                    else {
                        this.addClass('x-bare-field');
                    }
                }
            },
            style: 'width: 100%; background-color: #f6f6f6;',
            value: currentDescription
        });
        
        var nameArea = new Ext.form.TextArea({
            allowBlank: false,
            grow: true,
			growMin:1,
            growAppend: '',
            readOnly: true,//!this.editable,
            disabled: true,
            disabledClass: 'disabled-plain',
            emptyText: 'Name is required',
            enableKeyEvents: true,
            bubbleEvents: ['changeMade'],
            listeners: {
                'keyup': function(field, e){
                    if (field.isDirty()) {
                        field.fireEvent('changeMade', field.isValid());
                    }
                },
                'toggleEditMode': function(editOn){
                    this.setReadOnly(!editOn);
                    this.setDisabled(!editOn);
                    if (editOn) {
                        this.removeClass('x-bare-field');
                    }
                    else {
                        this.addClass('x-bare-field');
                    }
                }
            },
            style: 'font-weight: bold; font-size:1.3em; width:100%',
            value: currentName
        });
        
        resetEditableFields = function(){
            nameArea.setValue(currentName);
            descriptionArea.setValue(currentDescription);
            saveBtn.disable();
            cancelBtn.disable();
        };
        
        var editBtn = new Ext.Button({
            // would like to use on/off slider or swtich type control here
            text: 'Start editing',
            editOn: false,
            disabled: !this.editable,
            handler: function(button, event){
                this.fireEvent('toggleEditMode', true);
            },
            scope: this
        });
        var cancelBtn = new Ext.Button({
            text: 'Cancel',
            disabled: true,
            toolTip: 'Reset all fields to saved values',
            handler: function(){
                this.fireEvent('toggleEditMode', false);
            },
            scope: this
        });
        
        var saveBtn = new Ext.Button({
            text: 'Save',
            disabled: true,
            handler: function(){
                save();
                this.fireEvent('toggleEditMode', false);
            }
			,scope: this
        });

        var deleteEEButton = new Ext.Button({
            text: 'Delete Experiment Set',
            icon: '/Gemma/images/icons/cross.png',
            toolTip: 'Delete the experiment from the system',
            disabled: !this.editable,
            handler: this.deleteExperimentSet,
            scope: this
        });
        
        this.on('toggleEditMode', function(editOn){
            // is there a way to make this even propagate to all children automatically?
			this.editModeOn = editOn; // needed to warn user before tab change
            editBtn.setText((editOn) ? 'Editing mode on' : 'Start editing');
            editBtn.setDisabled(editOn);
            nameArea.fireEvent('toggleEditMode', editOn);
            descriptionArea.fireEvent('toggleEditMode', editOn);
            resetEditableFields();
            saveBtn.setDisabled(!editOn);
            cancelBtn.setDisabled(!editOn);
			if(!editOn){
                resetEditableFields();
				this.dirtyForm = false;
			}
        });
        
        this.on('changeMade', function(wasValid){
            // enable save button
            saveBtn.setDisabled(!wasValid);
            cancelBtn.setDisabled(!wasValid);
			this.dirtyForm = true;
            
        });
        var basics = new Ext.Panel({
			flex:0,
			width:'100%',
            ref: 'fieldPanel',
            collapsible: false,
            bodyBorder: false,
            frame: false,
            baseCls: 'x-plain-panel',
            bodyStyle: 'padding:10px',
            defaults: {
                bodyStyle: 'vertical-align:top;font-size:12px;color:black',
                baseCls: 'x-plain-panel',
                fieldClass: 'x-bare-field'
            },
            tbar: new Ext.Toolbar({
                hidden: !this.editable,
                items: [editBtn, ' ', saveBtn, ' ', cancelBtn, '-', deleteEEButton]
            }),
            items: [nameArea, descriptionArea, {
                layout: 'form',
                defaults: {
                    border: false
                },
                items: [{
                    fieldLabel: "Taxon",
                    html: e.taxonName
                }, {
                    fieldLabel: 'Experiments',
                    html: this.renderNumber(e)
                }, {
                    fieldLabel: 'Status',
                    html: this.renderStatus(e)
                }]
            }]
        });
        
        this.add(basics);
        
		/*MEMBERS GRID*/
		var experimentMembersGrid = new Gemma.ExpressionExperimentMembersGrid({
			title:'Experiment Set Members',
			name: 'experimentMembersGrid',
			hideHeaders: true,
			frame: true,
			experimentGroupId: e.id,
			selectedExperimentSetValueObject: e,
			groupName: e.name,
			allowSaveToSession: false,
			allowRemovals: this.editable,
			allowAdditions: this.editable,
			sortableColumnsView:true,
			hideCancel:true,
			showSeparateSaveAs:true,
			enableSaveOnlyAfterModification: true,
			flex: 1
		});
		
		/*this.experimentMembersGrid.loadMask = new Ext.LoadMask(this.experimentMembersGrid.getEl(), {
					msg : "Loading experiments ..."
				});
		this.experimentMembersGrid.loadMask.show();*/
		
		experimentMembersGrid.loadExperiments(e.expressionExperimentIds, function() {
					//this.experimentMembersGrid.loadMask.hide();
				}.createDelegate(this, [], false));
			
						
		experimentMembersGrid.on('experimentListSavedOver',function(){
			Ext.getBody().mask('Reloading set');
			// could just update experiment count, but this is easier for now since we'll probably change the tab layout soon
			window.location.reload();
		});
				
		experimentMembersGrid.on('experimentListCreated',function(id){
			Ext.getBody().mask('Loading new set');
			window.location = "/Gemma/expressionExperimentSet/showExpressionExperimentSet.html?id="+id;
		});	
		
		this.add(experimentMembersGrid);
		/* EO member's grid */
				
				
				
		// adjust when user logs in or out
		Gemma.Application.currentUser.on("logIn", function(userName, isAdmin){
			var appScope = this;
			ExpressionExperimentSetController.canCurrentUserEditGroup(this.experimentDetails.id, {
				callback: function(editable){
					appScope.adjustForIsEditable(editable);
				},
				scope: appScope
			});
			
		},this);
		Gemma.Application.currentUser.on("logOut", function(){
			this.adjustForIsEditable(false);
			// TODO reset widget if experiment is private!
		},this);
		
        this.doLayout();
        this.fireEvent("ready");
        
    }, // end of initComponent
    adjustForIsEditable: function(editable){
		this.fieldPanel.getTopToolbar().setVisible(editable);
	},
	deleteExperimentSet: function(){
		var id = this.experimentSet.id;
		var redirectHome = true;
		Ext.Msg.show({
					title : 'Delete '+this.experimentSet.name+'?',
					msg : 'Are you sure you want to delete experiment set "'+this.experimentSet.name+'"? This cannot be undone.',
					buttons : Ext.Msg.YESNO,
					fn : function(btn, text) {
						if (btn == 'yes') {
							var callParams = [];
							callParams.push([{id:id}]);
							if (!this.deleteMask) {
								this.deleteMask = new Ext.LoadMask(Ext.getBody(), {
									msg: "Deleting ..."
								});
							}
							this.deleteMask.show();
							callParams.push({
										callback : function(data) {
											window.location = '/Gemma/home.html';
										}.createDelegate(this),
										errorHandler : function(error) {
											Ext.Msg.alert("Deletion failed", error);
											this.deleteMask.hide();
										}.createDelegate(this)
									});
							ExpressionExperimentSetController.remove.apply(this, callParams);
						}
					},
					scope : this,
					animEl : 'elId',
					icon : Ext.MessageBox.WARNING
				});
	}
});

