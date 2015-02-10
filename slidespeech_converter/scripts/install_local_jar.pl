$mvn = "mvn";

$location = "/home/developer/git/slidespeech_converter/slidespeech_converter/local_maven_repository/";

system("$mvn install:install-file -Dfile=$location/ridl-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=ridl -Dpackaging=jar -Dversion=3.5.0");
system(" $mvn install:install-file -Dfile=$location/juh-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=juh -Dpackaging=jar -Dversion=3.5.0");
system(" $mvn install:install-file -Dfile=$location/unoil-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=unoil -Dpackaging=jar -Dversion=3.5.0");
system(" $mvn install:install-file -Dfile=$location/java_uno-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=java_uno -Dpackaging=jar -Dversion=3.5.0");
system(" $mvn install:install-file -Dfile=$location/libintl-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=libintl -Dpackaging=jar -Dversion=3.5.0");
system("$mvn install:install-file -Dfile=$location/jurt-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=jurt -Dpackaging=jar -Dversion=3.5.0");
system($mvn install:install-file -Dfile=$location/unoloader-3.5.0.jar -DgroupId=org.libreoffice -DartifactId=unoloader -Dpackaging=jar -Dversion=3.5.0")