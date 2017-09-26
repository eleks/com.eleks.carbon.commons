/**/

import groovy.transform.CompileStatic;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;

import org.wso2.carbon.registry.core.ResourceImpl;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.OMElement;
import javax.xml.namespace.QName;


def deploy(){
	//assume filename without extension equals to id...
	//let's put it into ctx to be available in undeploy
	ctx.name     = ((File)ctx.file).name.replaceAll('\\.[^\\.]*$','')
	String text = ((File)ctx.file).getText("UTF-8")
	OMElement xml = AXIOMUtil.stringToOM(text);

	String path = xml.getAttributeValue( new QName("path") );
	if(!path.endsWith("/"))path=path+"/";

	CarbonContext cc = CarbonContext.getThreadLocalCarbonContext();
	ctx.reg = cc.getRegistry( RegistryType.valueOf( xml.getAttributeValue( new QName("type") ) ) );

	OMElement child = xml.getFirstElement();
	if(child){
		text = child.toString();
	}else{
		text = xml.getText();
	}

	//now let's store text as a resource

	ctx.path = path + ctx.name; //combine a full path
	ResourceImpl resource = new ResourceImpl();
	resource.setContent(text);
	ctx.reg.put(ctx.path, resource);
}

def undeploy(){
	//assume that policyId was set in deploy
	//deletePolicy((String)ctx.policyId)
	ctx.reg.delete(ctx.path)
}

