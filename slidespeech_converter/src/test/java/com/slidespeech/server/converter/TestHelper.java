package com.slidespeech.server.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.jboss.arquillian.container.test.impl.client.deployment.tool.ToolingDeploymentFormatter;
import org.jboss.marshalling.OutputStreamByteOutput;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import com.slidespeech.server.converter.util.SshHelper;

public class TestHelper
{
	static String folderPrefix = "/presentation/";
	
	static String destionationFolder1OnVM = folderPrefix + "/testFolder1/";
	static String destionationFolder2OnVM = folderPrefix + "testFolder2/";
	static String destionationFolder3OnVM = folderPrefix + "/testFolder3/";
	static String destionationFolder4OnVM = folderPrefix + "/testFolder4/";
	static String destionationFolder5OnVM = folderPrefix + "/testFolder5/";
	static String destionationFolder6OnVM = folderPrefix + "/testFolder6/";
	static String destionationFolder7OnVM = folderPrefix + "/testFolder7/";
	static String destionationFolder8OnVM = folderPrefix + "/testFolder8/";

	static String local_presentation_dir = "./resource/test_presentations/";

	public static void copyTestPresentationsToVm(String sourcePAth,
			String destinationPathOnVm)
	{
		File dir = new File(sourcePAth);

		String[] children = dir.list();
		if (children == null)
		{
			// Either dir does not exist or is not a directory

		} else
		{
			for (int i = 0; i < children.length; i++)
			{
				// Get filename of file or directory
				String filename = children[i];

				File file = new File(dir + "/" + filename);

				assert (file.exists());

				if (filename.indexOf("svn") < 1)
				{
					System.out.println("copy " + filename + " to "
							+ destinationPathOnVm + " \n");

					String[] params =
					{ sourcePAth + "/" + filename,
							"root@slidespeech.squeeze:" + destinationPathOnVm,
							"alabama" };
					SshHelper.scp(params);
				}
			}
		}
	}

	public static Archive<?> createOneInstanceTestArchive()
	{
		copyTestResourcesTo(destionationFolder1OnVM);

		return createArchive();
	}

	public static Archive<?> createMultipleInstanceTestArchive()
	{

		copyTestResourcesTo(destionationFolder1OnVM);
		copyTestResourcesTo(destionationFolder2OnVM);
		copyTestResourcesTo(destionationFolder3OnVM);
		copyTestResourcesTo(destionationFolder4OnVM);
		copyTestResourcesTo(destionationFolder5OnVM);
		copyTestResourcesTo(destionationFolder6OnVM);
		copyTestResourcesTo(destionationFolder7OnVM);
		copyTestResourcesTo(destionationFolder8OnVM);

		return createArchive();
	}

	private static void copyTestResourcesTo(String destionationFolder)
	{

		SshHelper.executeSshCommand("slidespeech.squeeze", "root", "alabama",
				"rm -r " + destionationFolder);

		SshHelper.executeSshCommand("slidespeech.squeeze", "root", "alabama",
				"mkdir -p " + destionationFolder);
		SshHelper.executeSshCommand("slidespeech.squeeze", "root", "alabama",
				"chmod 777 " + destionationFolder);

		TestHelper.copyTestPresentationsToVm(local_presentation_dir,
				destionationFolder);

	}

//	private static Archive<?> createArchive()
//	{
////		MavenDependencyResolver resolver = DependencyResolvers.use(
////				MavenDependencyResolver.class).loadMetadataFromPom("pom.xml");
//
////		PomEquippedResolveStage importRuntimeAndTestDependencies = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies();
////		PomEquippedResolveStage loadPomFromFile = Maven.resolver().loadPomFromFile("pom.xml");
//		
////		MavenFormatStage withoutTransitivity = Maven.resolver().loadPomFromFile("pom.xml").resolve().withoutTransitivity();
////
////		File[] asFile = withoutTransitivity.asFile();
//		
//		WebArchive archive = ShrinkWrap
//				.create(WebArchive.class,"myarchive.jar");
//		
//		archive.addPackage("com.slidespeech.server.converter")
//		.addPackage("com.slidespeech.server.model")
//		.addPackage("com.slidespeech.server.message")
//		.addPackage("com.slidespeech.server.converter.openoffice")
//		.addPackage("com.slidespeech.server.service").
//		addPackage("com.slidespeech.server.spconverter");
//				
//		File[] asFile = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve().withTransitivity().asFile(); 
//		
//		for (File file : asFile) 
//		{
//			System.out.println(file.getAbsolutePath());
//			archive.addAsLibraries(file);
//		}
//		
//		
////		create.addAsLibraries(
////				resolver.artifact("com.artofsolving:jodconverter")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact(
////						"org.codehaus.jackson:jackson-core-asl")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact("org.apache.pdfbox:pdfbox")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact(
////						"org.codehaus.jackson:jackson-mapper-asl")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact("org.apache.commons:commons-io")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact("org.apache.commons:commons-exec")
////						.resolveAsFiles())
////		.addAsLibraries(
////				resolver.artifact("org.imgscalr:imgscalr-lib")
////						.resolveAsFiles());
////		
//		
//
//		
////				File[] asFile2 = Maven.resolver().resolve("com.artofsolving:jodconverter:2.2.2").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////					
////				}
////				
////				asFile2 = Maven.resolver().resolve("org.codehaus.jackson:jackson-core-asl:1.9.5").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////					
////				}
////		
////				Maven.resolver().resolve("org.apache.pdfbox:pdfbox:1.7.1").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////					
////				}
////				Maven.resolver().resolve("org.codehaus.jackson:jackson-mapper-asl:1.9.5").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////					
////				}
////				
////				Maven.resolver().resolve("org.apache.commons:commons-io:1.3.2").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////				}
////				
////				Maven.resolver().resolve("org.apache.commons:commons-exec:1.1").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////				}
////				
////				Maven.resolver().resolve("org.imgscalr:imgscalr-lib:4.2").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////				}
////				
////				Maven.resolver().resolve("org.libreoffice:ridl:3.5.0").withTransitivity().asFile();
////				
////				for (File file : asFile2) 
////				{
////					create.addAsLibraries(file);
////				}
//				
//				
//				
////	asFile2 = Maven.resolver().resolve("com.artofsolving:jodconverter:2.2.2").withTransitivity().asFile();
////				
////				for (File file : asFile) 
////				{
////					create.addAsLibraries(file);
////					
////				}
////	asFile2 = Maven.resolver().resolve("com.artofsolving:jodconverter:2.2.2").withTransitivity().asFile();
////				
////				for (File file : asFile) 
////				{
////					create.addAsLibraries(file);
////					
////				}
//		return archive; 
//	}

	public static Archive<?> createOneInstance2TestArchive() 
	{
		
		copyTestResourcesTo(destionationFolder1OnVM);
		copyTestResourcesTo(destionationFolder2OnVM);
		copyTestResourcesTo(destionationFolder3OnVM);
		copyTestResourcesTo(destionationFolder4OnVM);
		copyTestResourcesTo(destionationFolder5OnVM);
		copyTestResourcesTo(destionationFolder6OnVM);
		copyTestResourcesTo(destionationFolder7OnVM);
		copyTestResourcesTo(destionationFolder8OnVM);
		
		
		return createArchive();
	}

private static Archive<?> createArchive() {
	WebArchive archive = ShrinkWrap
			.create(WebArchive.class);
	
	File[] asFile = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve().withTransitivity().asFile(); 
	
	for (File file : asFile) 
	{
		archive.addAsLibraries(file);
	}
	
	archive.addPackage("com.slidespeech.server.converter")
	.addPackage("com.slidespeech.server.model")
	.addPackage("com.slidespeech.server.message")
	.addPackage("com.slidespeech.server.converter.openoffice")
	.addPackage("com.slidespeech.server.service").
	addPackage("com.slidespeech.server.spconverter");
	
	
	return archive;
}
}








