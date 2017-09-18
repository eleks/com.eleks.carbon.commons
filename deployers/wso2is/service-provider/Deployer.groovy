/**/
//import com.eleks.carbon.commons.util.CarbonUtil;
//import com.eleks.carbon.commons.util.HTTP;


import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
//import org.wso2.carbon.identity.application.mgt.ApplicationManagementAdminService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;

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
	//get domain and user (required to register service-provider) 
	ctx.tenant = sp.getOwner()?.getTenantDomain() ?: CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
	ctx.user   = sp.getOwner()?.getUserName()     ?: CarbonContext.getThreadLocalCarbonContext().getUsername() ?: "admin";
	if(ctx.user.indexOf('/')<0){
		ctx.user = (sp.getOwner()?.getUserStoreDomain() ?: 'PRIMARY')+'/'+ctx.user;
	}
	//get current service-provider by name
	def oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.tenant);
	if(!oldSp){
		//we have new SP but Application/role could exist. try to drop it..
		try{
			ApplicationMgtUtil.deleteAppRole(ctx.appName);
			log.debug "    ${ctx.appName} try to delete role.."
		}catch(e){ log.warn "    ${ctx.appName}" + e }
		//create new empty service provider if not exists with only name and description set
		oldSp = new ServiceProvider()
		oldSp.applicationName = sp.applicationName
		oldSp.description     = sp.description
		ctx.applicationService.createApplication(oldSp, ctx.tenant, ctx.user);
		oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.tenant);
		log.info "    ${ctx.appName} service-provider created with id: ${oldSp.applicationID}"
	}
	//get id of the existing service-provider
	sp.applicationID = oldSp.applicationID;
	//update all the parameters of service provider
	ctx.applicationService.updateApplication(sp, ctx.tenant, ctx.user);
	log.info "    ${ctx.appName} service-provider updated with id: ${sp.applicationID}"
}

def undeploy(){
	def oldSp = ctx.applicationService.getApplicationExcludingFileBasedSPs(ctx.appName,ctx.tenant);
	if(oldSp){
		ctx.applicationService.deleteApplication(ctx.appName, ctx.tenant, ctx.user);
		log.info "    ${ctx.appName} service-provider deleted"
	}
		
}

