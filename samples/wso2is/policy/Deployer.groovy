/**/
//import com.eleks.carbon.commons.util.CarbonUtil;
//import com.eleks.carbon.commons.util.HTTP;


import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.EntitlementPolicyAdminService;

//import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;

def deploy(){
	ctx.entitlementPolicyAdminService = new EntitlementPolicyAdminService()
	//assume filename without extension equals to id...
	//let's put it into ctx to be available in undeploy
	ctx.policyId = ctx.file.name.replaceAll('\\.[^\\.]*$','')
	def policyXml = ctx.file.getText("UTF-8")
	addOrUpdatePolicy(ctx.policyId, policyXml)
}

def undeploy(){
	//assume that policyId was set in deploy
	deletePolicy(ctx.policyId)
}

def deletePolicy(String id){
	ctx.entitlementPolicyAdminService.removePolicy(id, true);
	log.info "policy $id deleted"
	return true;
}

def addOrUpdatePolicy(String id, String policyXml){
	def policy = new PolicyDTO();
	policy.setPolicy(policyXml);
	policy.setActive(true); // set policy enabled
	if(getPolicy(id)){
		ctx.entitlementPolicyAdminService.updatePolicy(policy);
		log.info "policy $id updated"
	}else{
		ctx.entitlementPolicyAdminService.addPolicy(policy);
		log.info "policy $id created"
	}
	ctx.entitlementPolicyAdminService.publishToPDP([id] as String[], "CREATE", null, true, -1);
	log.info "policy $id published"
	return true
}

/**returns "pap" or "pdp" if policy exists. otherwose returns null*/
def getPolicy(String id){
	try{
		PolicyDTO policy = ctx.entitlementPolicyAdminService.getPolicy(id,true)
		assert policy.policyId==id
		return policy
	}catch(e){}
	try{
		PolicyDTO policy = ctx.entitlementPolicyAdminService.getPolicy(id,false)
		assert policy.policyId==id
		return policy
	}catch(e){}
	return null
}


