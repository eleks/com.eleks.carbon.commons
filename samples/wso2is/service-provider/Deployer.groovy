/**/
//import com.eleks.carbon.commons.util.CarbonUtil;
//import com.eleks.carbon.commons.util.HTTP;


import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
//import org.wso2.carbon.identity.application.mgt.ApplicationManagementAdminService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import org.wso2.carbon.context.CarbonContext;

//to parse xml to AXIOM
import org.apache.axiom.om.OMXMLBuilderFactory;

def deploy(){
	ctx.applicationService = ApplicationManagementService.getInstance();
	//assume filename without extension equals to service-provoder name...
	//let's put it into ctx to be available in undeploy
	ctx.appName = ctx.file.name.replaceAll('\\.[^\\.]*$','');
	//let's parse xml to axiom
	def omStream = ctx.file.newInputStream();
	def omElement = OMXMLBuilderFactory.createOMBuilder( omStream ).getDocumentElement();
	//build ServiceProvider from om element
	def sp = ServiceProvider.build(omElement);
	//release resources
	omStream.close();
	omStream = null;
	//call get 
	ctx.domain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
	ctx.user = CarbonContext.getThreadLocalCarbonContext().getUsername() ?: "PRIMARY/admin";
	log.info "    -----------------------------------------------------------------"

	//>>> fix permissions
	/*
	def permissions = sp.getPermissionAndRoleConfig().getPermissions()
	for(int i=0;i<permissions.length;i++){
		def value = permissions[i].getValue()
		permissions[i] = new ApplicationPermission(){
			public String toString(){
				return this.getValue()+"/.";
			}
		}
		permissions[i].setValue(value);
	}
	sp.getPermissionAndRoleConfig().setPermissions(permissions)
	*/
	//<<< fix permissions
	
	def oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.domain);
	if(!oldSp){
		//create new empty service provider
		oldSp = new ServiceProvider()
		oldSp.applicationName = sp.applicationName
		oldSp.description     = sp.description
		ctx.applicationService.createApplication(oldSp, ctx.domain, ctx.user);
		oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.domain);
		log.info "    ${ctx.appName} service-provider created with id: ${oldSp.applicationID}"
	}
	//get id of the existing service-provider
	sp.applicationID = oldSp.applicationID;
    new File("SP.JSON").setText( groovy.json.JsonOutput.toJson( sp ) )
	ctx.applicationService.updateApplication(sp, ctx.domain, ctx.user);
	log.info "    ${ctx.appName} service-provider updated with id: ${sp.applicationID}"
}

def undeploy(){
	def oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.domain);
	if(oldSp){
		ctx.applicationService.deleteApplication(ctx.appName, ctx.domain, ctx.user);
		log.info "    ${ctx.appName} service-provider deleted"
	}
		
}

