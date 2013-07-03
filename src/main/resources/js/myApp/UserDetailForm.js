  var user_uid;
  var editUserFlag=true;
  Bus = new Ext.util.Observable();

  var brukernavn_field = new Ext.form.TextField({
        fieldLabel: 'Brukernavn',
//        disabled: true,
        readOnly: true,
        name: 'username',
        anchor:'-15'
    });

  var firstName_field = new Ext.form.TextField({
        fieldLabel: 'Fornavn',
//        disabled: true,
        readOnly: true,
        name: 'firstName',
        anchor:'-15'
    });
  var lastname_field = new Ext.form.TextField({
        fieldLabel: 'Etternavn',
//        disabled: true,
        readOnly: true,
        name: 'lastName',
        anchor:'-15'
    });
  var email_field = new Ext.form.TextField({
        fieldLabel: 'epost',
//        disabled: true,
        readOnly: true,
        name: 'email',
        anchor:'-15'
    });
  var cell_field = new Ext.form.TextField({
        fieldLabel: 'Mobil',
//        disabled: true,
        readOnly: true,
        name: 'cellPhone',
        anchor:'-15'
    });

   var addButtonHandler = function(button,event) {
         editUserFlag=false;
         new_user_uid=''; //Commented by Rafal 
         new_user_brukernavn='';
         new_user_firstName='';
         new_user_lastName='';
         new_user_email='';
         new_user_cell='';
         showModalUserDetail();
   };
   var editButtonHandler = function(button,event) {
         editUserFlag=true;
         new_user_uid=myJsonIdentityStore.data.items[0].get('uid');
         new_user_brukernavn=myJsonIdentityStore.data.items[0].get('username');
         new_user_firstName=myJsonIdentityStore.data.items[0].get('firstName');
         new_user_lastName=myJsonIdentityStore.data.items[0].get('lastName');
         new_user_email=myJsonIdentityStore.data.items[0].get('email');
         new_user_cell=myJsonIdentityStore.data.items[0].get('cellPhone');
         showModalUserDetail();
   };

   var saveButtonHandler = function(button,event) {
        if (editUserFlag) {
            e_brukernavn_field.setValue(new_user_brukernavn);
            editUser();
        } else {
        	addUser();
        	
        }
        eudwin.hide();
        reloadForm();
   };
   var cancelButtonHandler = function(button,event) {
        eudwin.hide();
        reloadForm();
   };
   
   
   function addUser() {
            //   e_uid_field.setValue(new_user_uid);
	   var userjson = '{\"personRef\":\"0000000000\", \"username\":\"'+e_brukernavn_field.getValue()+
                '\", \"firstName\":\"'+e_firstName_field.getValue()+'\", \"lastName\":\"'+e_lastName_field.getValue()+
                '\", \"email\":\"'+e_email_field.getValue()+'\", \"cellPhone\":\"'+e_cell_field.getValue()+'\"}';

    
        Ext.Ajax.defaultHeaders = { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' };
        Ext.Ajax.request({
            url: myHostUserAdd+'&jsond='+userjson, 
	   	    method: 'GET',

	      	//jsonData: jsonStr,  // your json data
            //jsonData: Ext.encode(mjobj),
	   	 
            success: function(transport){
                myJsonRoleDataStore.load();
            },
            failure: function(transport){
                alert("Fikk ikke oprettet brukeren: " - transport.responseText);
                myJsonRoleDataStore.load();
            }
        });
   };

   function editUser() {
            //   e_uid_field.setValue(new_user_uid);

        var userjson = '{\"personRef\":\"'+myJsonIdentityStore.data.items[0].get('personRef')+'\", \"username\":\"'+e_brukernavn_field.getValue()+
                '\", \"firstName\":\"'+e_firstName_field.getValue()+'\", \"lastName\":\"'+e_lastName_field.getValue()+
                '\", \"email\":\"'+e_email_field.getValue()+'\", \"cellPhone\":\"'+e_cell_field.getValue()+'\"}';

        Ext.Ajax.defaultHeaders = { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' };
        Ext.Ajax.request({
           url: myHostJsonUserUpdate+new_user_brukernavn+'&jsond='+userjson,
	   	    method: 'GET',
            success: function(transport){
                myJsonRoleDataStore.load();
            },
            failure: function(transport){
                alert("Fikk ikke oprettet brukeren: " - transport.responseText);
                myJsonRoleDataStore.load();
            }
        });
   };


   function reloadForm() {
        search_uid = e_brukernavn_field.getValue();
        newUrl = myHostJsonUsers+search_uid;
        myJsonIdentityStore.proxy.conn.url = newUrl;
        myJsonIdentityStore.load();
        myJsonApplicationDataStore.proxy.conn.url = newUrl+'/applications';
        myJsonApplicationDataStore.load();
        myJsonRoleDataStore.proxy.conn.url = newUrl;
        myJsonRoleDataStore.load();
   };

   var new_user_uid;
   var new_user_brukernavn;
   var new_user_firstName;
   var new_user_lastName;
   var new_user_email;
   var new_user_cell;

   var eudwin = new Ext.Window({
           layout:'fit',
           width:400,
           title:"Editer bruker",
           shim :false,
           modal:true,
           autoDestroy :true,
           monitorValid:true,
           closable:false,
           resizable:false,
           buttons: [{text: 'Avbryt', handler: cancelButtonHandler},
                      {text:'OK', handler: saveButtonHandler}],
           items:[{ title:'Bruker detaljer',
                id: 'brukerdetaljer',
                height:225,
                layout: 'fit',
                xtype: 'edituserpanel'
           }]
   });

   function showModalUserDetail() {
            e_uid_field.setValue(new_user_uid);
            e_brukernavn_field.setValue(new_user_brukernavn);
            e_firstName_field.setValue(new_user_firstName);
            e_lastName_field.setValue(new_user_lastName);
            e_email_field.setValue(new_user_email);
            e_cell_field.setValue(new_user_cell);

            eudwin.show();
   };
   
   //var saveButtonHandler = function(button,event) {

   var deleteButtonHandler = function(button,event) {
                Ext.MessageBox.confirm('Bekreft', 'Er du sikker på at du vil slette '+user_uid+'?', showDeleteResult);
   };

   function showDeleteResult(btn){
        if (btn == 'yes') {
            var conn = new Ext.data.Connection();
            conn.request({
                url:        myHostUserDelete+user_uid+'/delete', //OLD
            	//url:        myHostUserDelete+user_uid, //New
                method:     'GET', //OLD
                success: function(responseObject) {
                    Ext.MessageBox.alert(user_uid+' - slettet ');
                },
                failure: function() {
                    Ext.MessageBox.alert('Something failed');
                }
            });
        }
   };

UserDetailForm = Ext.extend(Ext.form.FormPanel, {


    initComponent: function(config) {
        var config = {
            // Put your pre-configured config options here
            title: 'Brukerinformasjon',
            id: 'bruker-panel',
            bodyStyle:'padding:15px',
            monitorValid:true,
            autoScroll:true,
            labelWidth:70,
            store: myJsonIdentityStore,
            layout: 'form',
            items: [ brukernavn_field, firstName_field, lastname_field, email_field, cell_field ],
                bbar:  ['->', {
                            text: 'Ny bruker',
                            minWidth: 100,
                            iconCls:'add',
                            handler: addButtonHandler,
                            ref: '../cancelButton'
                        }, {
                            text: 'Endre bruker',
                            minWidth: 100,
                            iconCls:'edit',
                            handler: editButtonHandler,
                            ref: '../editButton'
                        }, {
                            text: 'Slett bruker',
                            minWidth: 100,
                            iconCls:'remove',
                            handler: deleteButtonHandler,
                            ref: '../deleteButton'
                        }
                ],
                iconCls:'icon-grid'



            }; // eo config
            Ext.apply(this, Ext.apply(this.initialConfig, config));
            UserDetailForm.superclass.initComponent.apply(this, arguments);
            Bus.on('message', this.onMessage, this);
        }
        ,onRender:function() {
            UserDetailForm.superclass.onRender.apply(this, arguments);
         }
        ,onMessage:function(message) {
//             alert(message);
//             myJsonIdentityStore.reload();
             user_uid = myJsonIdentityStore.data.items[0].get('uid');
    		 brukernavn_field.setValue(myJsonIdentityStore.data.items[0].get('username'));
    		 firstName_field.setValue(myJsonIdentityStore.data.items[0].get('firstName'));
    		 lastname_field.setValue(myJsonIdentityStore.data.items[0].get('lastName'));
    		 email_field.setValue(myJsonIdentityStore.data.items[0].get('email'));
    		 cell_field.setValue(myJsonIdentityStore.data.items[0].get('cellPhone'));
        }
   }
);


Ext.reg('userpanel', UserDetailForm);