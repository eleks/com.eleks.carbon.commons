/**/
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder


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
	ctx.policyId     = ((File)ctx.file).name.replaceAll('\\.[^\\.]*$','') //remove last extension
	String policyXml = ((File)ctx.file).getText("UTF-8")
	if(((File)ctx.file).name.endsWith(".json")){
		//convert yaml to xml
		policyXml = convertJson2Xml(policyXml, (String)ctx.policyId)
		//write calculated policy xml to the file
		//new File("${ctx.file}.policy").setText(policyXml, "UTF-8")
	}
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
	log.info "policy   $id deleted"
	return true;
}

@CompileStatic
def addOrUpdatePolicy(String id, String policyXml){
	def policy = new PolicyDTO();
	policy.setPolicy(policyXml);
	policy.setActive(true); // set policy enabled
	if(getPolicy(id)){
		Const.svc.updatePolicy(policy);
		log.info "policy   $id updated"
	}else{
		Const.svc.addPolicy(policy);
		log.info "policy   $id created"
	}
	Const.svc.publishToPDP([id] as String[], "CREATE", null, true, -1);
	log.info "policy   $id published"
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

def convertJson2Xml(String text, String policyId){
	def json = new JsonSlurper().parseText(text);
	def w = new StringWriter(text.length()*26)
	def mb = new MarkupBuilder(w)
	mb.setDoubleQuotes(true)
    mb."Policy"(xmlns:"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", PolicyId:policyId, RuleCombiningAlgId:"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable",Version:"1.0"){
        Target{AnyOf{
            json.role.each{r->
                AllOf{Match(MatchId:"urn:oasis:names:tc:xacml:1.0:function:string-equal"){
                    AttributeValue(DataType:"http://www.w3.org/2001/XMLSchema#string"){
                        mkp.yield(r)
                    }
                    AttributeDesignator(AttributeId:"http://wso2.org/claims/role",Category:"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",DataType:"http://www.w3.org/2001/XMLSchema#string",MustBePresent:"true")
                }}
            }
        }}
        Rule(Effect:"Permit",RuleId:"Rule-Permit"){
            Target{
                AnyOf{
                    json.actions.each{r,aa->
                        aa.each{a->
                            AllOf{
                                Match(MatchId:"urn:oasis:names:tc:xacml:1.0:function:string-equal"){
                                    AttributeValue(DataType:"http://www.w3.org/2001/XMLSchema#string",r)
                                    AttributeDesignator(AttributeId:"urn:oasis:names:tc:xacml:1.0:resource:resource-id",Category:"urn:oasis:names:tc:xacml:3.0:attribute-category:resource",DataType:"http://www.w3.org/2001/XMLSchema#string",MustBePresent:"true")
                                }
                                Match(MatchId:"urn:oasis:names:tc:xacml:1.0:function:string-equal"){
                                    AttributeValue(DataType:"http://www.w3.org/2001/XMLSchema#string",a)
                                    AttributeDesignator(AttributeId:"urn:oasis:names:tc:xacml:1.0:action:action-id",Category:"urn:oasis:names:tc:xacml:3.0:attribute-category:action",DataType:"http://www.w3.org/2001/XMLSchema#string",MustBePresent:"true")
                                }
                            }
                        }
                    }
                }
            }
        }
        Rule(Effect:"Deny",RuleId:"Rule-Deny")
    }
    return w.toString()
}

