## wso2 identity server policy deployer example
### deploy & configuration
#### com.eleks.carbon.commons.jar
put the latest `com.eleks.carbon.commons.jar` library from [releases](https://github.com/eleks/com.eleks.carbon.commons/releases) into `repository/components/lib` folder on wso2is server

#### axis2.xml 
add the following section into axis2.xml:

```xml
    <deployer extension="xml" directory="policy" 
        class="com.eleks.carbon.commons.deployer.GroovyDeployer" />
```

#### deployment folder

create `repository/deployment/server/policy` folder and put in it [`Deployer.groovy`](./Deployer.groovy)

#### result

after this you can put `xml` files with policy definition into this folder and it will be automatically created on server. 

### policy xml structure

in wso2is web console you can create the policy and edit it as xml.  

check this example: [kernelChiefAgronomist_master_policy.xml](./kernelChiefAgronomist_master_policy.xml) 
