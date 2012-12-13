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
                return profileParamValue == "jbpm";
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
//                        var pathjson = response.responseText.evalJSON();
//                        var pathobj = pathjson["paths"];
//                        for(var key in pathobj) {
//                            if(key == pathid) {
//                                var color = this.getDisplayColor(0);
//                                var val = pathobj[key];
//                                this.setNodeColors(key, color, val);
//                            }
//                        }
//                        this.facade.raiseEvent({
//                            type: ORYX.CONFIG.EVENT_SIMULATION_PATH_SVG_GENERATED
//                        });
                    } else {
//                        Ext.MessageBox.minWidth = 200;
//                        Ext.Msg.alert('Invalid Path data.');
                    }
                } catch(e) {
//                    Ext.MessageBox.minWidth = 200;
//                    Ext.Msg.alert('Error finding Paths:\n' + e);
                }
            }.bind(this),
            failure: function(){
//                Ext.Msg.alert('Error finding Paths.');
            },
            params: {
                action: 'updateasset',
                assetcontent: ORYX.EDITOR.getSerializedJSON(),
                pp: ORYX.PREPROCESSING,
                assetlocation: ORYX.UUID


            }
        });








        this.facade.raiseEvent({
            type 		: ORYX.CONFIG.EVENT_NOTIFICATION_SHOW,
            ntype		: 'success',
            msg         : 'Successfully saved Asset',
            title       : ''
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