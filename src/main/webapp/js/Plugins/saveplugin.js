if (!ORYX.Plugins)
    ORYX.Plugins = {};

if (!ORYX.Config)
    ORYX.Config = {};

ORYX.Plugins.SavePlugin = Clazz.extend({
    construct: function(facade){
        this.facade = facade;

        this.facade.offer({
            'name': ORYX.I18N.Save.save,
            'functionality': this.save.bind(this),
            'group': ORYX.I18N.Save.group,
            'icon': ORYX.PATH + "images/disk.png",
            'description': ORYX.I18N.Save.saveDesc,
            'index': 1,
            'minShape': 0,
            'maxShape': 0,
            'isEnabled': function(){
                profileParamName = "profile";
                profileParamName = profileParamName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
                regexSa = "[\\?&]"+profileParamName+"=([^&#]*)";
                regexa = new RegExp( regexSa );
                profileParams = regexa.exec( window.location.href );
                profileParamValue = profileParams[1];
                return profileParamValue == "jbpm" && ORYX.REPOSITORY_ID != "guvnor";
            }.bind(this)
        });
    },
    save : function() {

        Ext.Ajax.request({
            url: ORYX.PATH + 'assetservice',
            method: 'POST',
            success: function(response) {
                try {
                    if(response.responseText && response.responseText.length > 0) {
                        var saveResponse = response.responseText.evalJSON();
                        if(saveResponse.errors && saveResponse.errors.lengt > 0) {
                            var errors = saveResponse.errors;
                            for(var j=0; j < errors.length; j++) {
                                var errormessageobj = errors[j];
                                this.facade.raiseEvent({
                                    type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
                                    ntype		: 'error',
                                    msg         : errormessageobj.message,
                                    title       : ''
                                });
                            }
                        } else {
                            this.facade.raiseEvent({
                                type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
                                ntype		: 'success',
                                msg         : 'Successfully saved asset: ' + ORYX.UUID,
                                title       : ''
                            });
                        }
                    } else {
                        this.facade.raiseEvent({
                            type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
                            ntype		: 'error',
                            msg         : 'Unable to save: ' + e,
                            title       : ''
                        });
                    }
                } catch(e) {
                    this.facade.raiseEvent({
                        type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
                        ntype		: 'error',
                        msg         : 'Unable to save: ' + e,
                        title       : ''
                    });
                }
            }.bind(this),
            failure: function(){
                this.facade.raiseEvent({
                    type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
                    ntype		: 'error',
                    msg         : 'Unable to save.',
                    title       : ''
                });
            }.bind(this),
            params: {
                action: 'updateasset',
                assetContent: ORYX.EDITOR.getSerializedJSON(),
                pp: ORYX.PREPROCESSING,
                assetlocation: ORYX.UUID,
                assetcontenttransform: 'jsontobpmn2'
            }
        });
    }
});

window.onOryxResourcesLoaded = function() {
    var stencilset = ORYX.Utils.getParamFromUrl('stencilset') || ORYX.CONFIG.SSET;
    var editor_parameters = {
        id: ORYX.UUID,
        stencilset: {
            url: stencilset
        }
    };
    if(!(ORYX.UUID === undefined)) {

        //load the model from the repository from its uuid
        new Ajax.Request(ORYX.CONFIG.UUID_URL(), {
            asynchronous: false,
            encoding: 'UTF-8',
            method: 'get',
            onSuccess: function(transport) {
                response = transport.responseText;
                if (response.length != 0) {
                    try {
                        model = response.evalJSON();
                        editor_parameters.model = model;
                    } catch(err) {
                        ORYX.LOG.error(err);
                    }
                }

            },
            onFailure: function(transport) {
                ORYX.LOG.error("Could not load the model for uuid " + ORYX.UUID);
            }
        });
    }
    // finally open the editor:
    var editor = new ORYX.Editor(editor_parameters);
    ORYX.EDITOR = editor;
};