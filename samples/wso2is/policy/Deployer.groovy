/**/
import com.eleks.carbon.commons.util.CarbonUtil;
import com.eleks.carbon.commons.util.HTTP;


import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.EntitlementPolicyAdminService;

class Const{
	static String engpoint = "https://localhost:9443/services/EntitlementPolicyAdminService"
	static String authorization = "Basic "+"admin:admin".getBytes("UTF-8").encodeBase64()
	static EntitlementPolicyAdminService entitlementPolicyAdminService = new EntitlementPolicyAdminService()
}

def deploy(ctx){
	CarbonUtil.onServerStart{
		def policy = ctx.file.getText("UTF-8")
		addPolicy1(policy)
	}
}

def undeploy(ctx){
}

/*add policy using in-memory access*/
def addPolicy1(String policyXml){
	def policy = new PolicyDTO();
	policy.setPolicy(policyXml);
	Const.entitlementPolicyAdminService.addPolicy(policy);
	return true
}



/*add policy using http call*/
def addPolicy(String policy){
		def envelope = """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://dto.entitlement.identity.carbon.wso2.org/xsd">
   <soapenv:Header/>
   <soapenv:Body>
      <xsd:addPolicy>
         <xsd:policyDTO>
            <xsd1:policy><![CDATA[${policy}]]></xsd1:policy>
         </xsd:policyDTO>
      </xsd:addPolicy>
   </soapenv:Body>
</soapenv:Envelope>
		""" //end of envelope "
		def r = HTTP.post(
			url: Const.endpoint,
			body: envelope,
			encoding: "UTF-8",
			headers: [
				"SOAPAction"   : "\"urn:addPolicy\"",
				"Content-Type" : "text/xml;charset=UTF-8",
				"Authorization": Const.authorization,
			]
		).response
		assert r.code == 200
		assert r.body."*:Body"."*:addPolicyResponse"."*:return"
		return true
}

