## wso2 carbon any server registry items deployer
### deploy & configuration
#### libraries
put the latest `com.eleks.carbon.commons.jar` library from [releases](https://github.com/eleks/com.eleks.carbon.commons/releases) into `repository/components/lib` folder on wso2 server

make sure you have a groovy-all library in your server 

if not [download the latest](https://mvnrepository.com/artifact/org.codehaus.groovy/groovy-all) and put into `repository/components/lib` folder on wso2 server

#### axis2.xml 
add the following section into axis2.xml:

```xml
    <deployer extension="xreg" directory="registry" 
        class="com.eleks.carbon.commons.deployer.GroovyDeployer" />
```

#### deployment folder

create `repository/deployment/server/registry` folder and put in it [`Deployer.groovy`](./Deployer.groovy)

#### result

after this you can put `xreg` files with policy definition into this folder and it will be automatically created on server. 

### xreg file structure

xreg contains the only one root element

```xml
<registry type="..." path="..."><![CDATA[
content here
]]></registry>
```

where

- *type* : USER_CONFIGURATION,  USER_GOVERNANCE,  SYSTEM_CONFIGURATION,  SYSTEM_GOVERNANCE,  LOCAL_REPOSITORY
- *path* : a place where to store the file 
 the name of the file will be like original file but without `xreg` extension
 ex: `aaa.bbb.xreg` -> `aaa.bbb`


check xreg file examples in this folder

