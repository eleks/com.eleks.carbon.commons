/* api deployer */
import org.wso2.carbon.utils.CarbonUtils

def getAPIImportUtil(){
	GroovyClassLoader gcl = this.getClass().getClassLoader()
	try{
		//maybe class already loaded - just return it
		return gcl.loadClass("org.wso2.carbon.apimgt.importexport.utils.APIImportUtil").newInstance()
	}catch(e){}
	//lookup api-import-export web application root
	File webappRoot = new File( CarbonUtils.getCarbonHome()+"/repository/deployment/server/webapps" )
	File webappApiRoot = webappRoot.listFiles().find{ it.isDirectory() && it.getName().startsWith("api-import-export-") }
	assert webappApiRoot!=null : "`api-import-export-*` folder not found in $webappRoot"
	//modifyclasspath to load api-import-export classes
	gcl.addClasspath( "${webappApiRoot}/WEB-INF/classes" )
	//maybe we have to load all jars in webapp but they are absent for now...
	//try to load and instantiate again
	return gcl.loadClass("org.wso2.carbon.apimgt.importexport.utils.APIImportUtil").newInstance()
}

File tmpDir(String prefix){
	File root = new File( CarbonUtils.getCarbonHome()+"/tmp/api-import" )
	root.mkdirs()
	//root.deleteOnExit()
	while(true){
		File d = new File(root, prefix+"#"+Long.toHexString(System.currentTimeMillis()))
		if(!d.exists()){
			d.mkdirs()
			return d
		}
		Thread.sleep(31)
	}
}


def deploy(){
	String user = "admin"
	ctx.apiImp = getAPIImportUtil()
	File tmpDir = tmpDir( ctx.file.getName().replaceAll(/\.[^\.]*$/,"") )
	try{
		//provide filename without extension (zip) as prefix
		String extractedFolderName = ctx.apiImp.extractArchive(ctx.file, tmpDir.getPath());
		ctx.apiImp.initializeProvider(user);
		ctx.apiImp.importAPI( new File(tmpDir, extractedFolderName).getPath(), user, true);
		tmpDir.deleteDir()
	}catch(Throwable t){
		log.error "failed to import API from $tmpDir"
		throw t
		//keep directory to investigate the problem
	}
}

def undeploy(){
	//nothing to do...
}
