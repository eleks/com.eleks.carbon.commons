## wso2 identity server service-provider deployer example

#### com.eleks.carbon.commons.jar
put the latest `com.eleks.carbon.commons.jar` library into `repository/components/lib` folder on wso2is server

#### axis2.xml 
add the following section into axis2.xml:

```xml
    <deployer extension="xml" directory="service-provider" class="com.eleks.carbon.commons.deployer.GroovyDeployer" />
```

#### deployment folder

create `repository/deployment/server/service-provider` folder and put in it `Deployer.groovy`

#### result

after this you can put `xml` files with service-provider definition into this folder and it will be automatically created on server. 
