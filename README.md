
 **TeamCity buildlog processor plugin**

 This plugin parse Error and Text messages by regex format(default - "http.*://.*:.*@.*") and replace it with "*****".   
 The regex can be provided with "hide.regex" configuration parameter for specific build

 * **Build**  
 
 Run 'mvn clean package' command from the root project to build your plugin.  
 Resulting package <artifactId>.zip will be placed in ctco-buildlog-processor-teamcity-plugin module 'target' directory. 
 
 * **Install**  
 
 To install the plugin, put zip archive to 'plugins' dir under TeamCity data directory and restart the server.
 
 * **Deploy**

To Build and Deploy artifact to remote repository([docs](https://maven.apache.org/plugins/maven-deploy-plugin/deploy-mojo.html)):

mvn clean package deploy:deploy -DaltDeploymentRepository=serverId::default::https://hostname/repository
