## wso2 api manager api & app deployer

This deployer uses the `api-import-export-XXX.war` web application described here: https://docs.wso2.com/display/AM260/Migrating+the+APIs+and+Applications+to+a+Different+Environment


### setup & use

#### artifacts
- put the latest `com.eleks.carbon.commons.jar` library from [releases](https://github.com/eleks/com.eleks.carbon.commons/releases) into `repository/components/lib` folder on wso2am server
- put the groovy-all-XXX.jar library (tested on version 2.4.12) from any [maven repository](https://mvnrepository.com/artifact/org.codehaus.groovy/groovy-all) into `repository/components/lib` folder on wso2am server
- put the corresponding `api-import-export-XXX.war` into `repository/deployment/server/webapps` folder on wso2am server like [described here](https://docs.wso2.com/display/AM260/Migrating+the+APIs+and+Applications+to+a+Different+Environment). 

#### axis2.xml 
add the following section into axis2.xml near other `<deployer` sections:

```xml
    <!-- api & application import from zip file -->
    <deployer extension="zip" directory="api" class="com.eleks.carbon.commons.deployer.GroovyDeployer" />
```

#### deployment folder
create `repository/deployment/server/api` folder and put in it [`Deployer.groovy`](./Deployer.groovy)

this groovy script uses `api-import-export-XXX.war` web application to import APIs.

#### result
after this you can put `zip` file exported with [apimcli](https://docs.wso2.com/display/AM260/Migrating+the+APIs+and+Applications+to+a+Different+Environment) 
tool with api or application definition into this folder and it will be automatically imported into server through `api-import-export-*.war` like if you were using `apimcli`. 

