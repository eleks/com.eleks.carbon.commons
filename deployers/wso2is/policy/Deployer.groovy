/**/
//import com.eleks.carbon.commons.util.CarbonUtil;
//import com.eleks.carbon.commons.util.HTTP;


import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.EntitlementPolicyAdminService;
import groovy.transform.CompileStatic;

@CompileStatic
class Const{
	static EntitlementPolicyAdminService svc = new EntitlementPolicyAdminService()
}

//import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;

@CompileStatic
def deploy(){
	//ctx.entitlementPolicyAdminService = new EntitlementPolicyAdminService()
	//assume filename without extension equals to id...
	//let's put it into ctx to be available in undeploy
	ctx.policyId     = ((File)ctx.file).name.replaceAll('\\.[^\\.]*$','')
	String policyXml = ((File)ctx.file).getText("UTF-8")
	addOrUpdatePolicy((String)ctx.policyId, policyXml)
}

@CompileStatic
def undeploy(){
	//assume that policyId was set in deploy
	deletePolicy((String)ctx.policyId)
}

@CompileStatic
def deletePolicy(String id){
	Const.svc.removePolicy(id, true);
	log.info "policy $id deleted"
	return true;
}

@CompileStatic
def addOrUpdatePolicy(String id, String policyXml){
	def policy = new PolicyDTO();
	policy.setPolicy(policyXml);
	policy.setActive(true); // set policy enabled
	if(getPolicy(id)){
		Const.svc.updatePolicy(policy);
		log.info "policy $id updated"
	}else{
		Const.svc.addPolicy(policy);
		log.info "policy $id created"
	}
	Const.svc.publishToPDP([id] as String[], "CREATE", null, true, -1);
	log.info "policy $id published"
	return true
}

/**returns "pap" or "pdp" if policy exists. otherwose returns null*/
@CompileStatic
def getPolicy(String id){
	try{
		PolicyDTO policy = Const.svc.getPolicy(id,true)
		assert policy.policyId==id
		return policy
	}catch(e){}
	try{
		PolicyDTO policy = Const.svc.getPolicy(id,false)
		assert policy.policyId==id
		return policy
	}catch(e){}
	return null
}


