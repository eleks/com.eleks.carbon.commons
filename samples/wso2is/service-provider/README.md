## wso2 identity server service-provider deployer example

### deployment & configuration

#### com.eleks.carbon.commons.jar
put the latest `com.eleks.carbon.commons.jar` library from [releases](https://github.com/eleks/com.eleks.carbon.commons/releases) into `repository/components/lib` folder on wso2is server

#### axis2.xml 
add the following section into axis2.xml:

```xml
    <deployer extension="xml" directory="service-provider" 
        class="com.eleks.carbon.commons.deployer.GroovyDeployer" />
```

#### deployment folder

create `repository/deployment/server/service-provider` folder and put in it `Deployer.groovy`

#### result

after this you can put `xml` files with service-provider definition into this folder and it will be automatically created on server. 

### the structure of service-provider xml file
There is no xsd file =( from wso2 and to understand the structure you have to see the following method:

[ServiceProvider.build(OMElement serviceProviderOM)](https://github.com/wso2-attic/carbon-identity/blob/master/components/application-mgt/org.wso2.carbon.identity.application.common/src/main/java/org/wso2/carbon/identity/application/common/model/ServiceProvider.java#L59)

As an example please see the configuration: [BServiceProvider.xml](./BServiceProvider.xml)
