package org.sagebionetworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseServiceException;
import org.sagebionetworks.client.exceptions.SynapseUserException;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

public class IT054FileEntityTest {
	
	public static final long MAX_WAIT_MS = 1000*10; // 10 sec
	
	private static String FILE_NAME = "LittleImage.png";
	private static String FILE_NAME_2 = "profile_pic.png";

	private static Synapse synapse = null;
	File imageFile;
	File imageFile2;
	S3FileHandle fileHandle;
	S3FileHandle fileHandle2;
	PreviewFileHandle previewFileHandle;
	Project project;
	
	private static Synapse createSynapseClient(String user, String pw) throws SynapseException {
		Synapse synapse = new Synapse();
		synapse.setAuthEndpoint(StackConfiguration
				.getAuthenticationServicePrivateEndpoint());
		synapse.setRepositoryEndpoint(StackConfiguration
				.getRepositoryServiceEndpoint());
		synapse.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		synapse.login(user, pw);
		
		return synapse;
	}
	
	/**
	 * @throws Exception
	 * 
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		synapse = createSynapseClient(StackConfiguration.getIntegrationTestUserOneName(),
				StackConfiguration.getIntegrationTestUserOnePassword());
		// Create a 
	}
	
	@Before
	public void before() throws SynapseException {
		// Get the image file from the classpath.
		URL url = IT054FileEntityTest.class.getClassLoader().getResource("images/"+FILE_NAME);
		imageFile = new File(url.getFile().replaceAll("%20", " "));
		assertNotNull(imageFile);
		assertTrue(imageFile.exists());
		url = IT054FileEntityTest.class.getClassLoader().getResource("images/"+FILE_NAME_2);
		imageFile2 = new File(url.getFile().replaceAll("%20", " "));
		assertNotNull(imageFile2);
		assertTrue(imageFile2.exists());
		// Create the image file handle
		List<File> list = new LinkedList<File>();
		list.add(imageFile);
		list.add(imageFile2);
		FileHandleResults results = synapse.createFileHandles(list);
		assertNotNull(results);
		assertNotNull(results.getList());
		assertEquals(2, results.getList().size());
		fileHandle = (S3FileHandle) results.getList().get(0);
		fileHandle2 = (S3FileHandle) results.getList().get(1);
		// Create a project, this will own the file entity
		project = new Project();
		project = synapse.createEntity(project);
	}

	/**
	 * @throws Exception 
	 * @throws HttpException
	 * @throws IOException
	 * @throws JSONException
	 * @throws SynapseUserException
	 * @throws SynapseServiceException
	 */
	@After
	public void after() throws Exception {
		if(project != null){
			synapse.deleteEntity(project);
		}
		if(fileHandle != null){
			try {
				synapse.deleteFileHandle(fileHandle.getId());
			} catch (Exception e) {}
		}
		if(fileHandle2 != null){
			try {
				synapse.deleteFileHandle(fileHandle2.getId());
			} catch (Exception e) {}
		}
		if(previewFileHandle != null){
			try {
				synapse.deleteFileHandle(previewFileHandle.getId());
			} catch (Exception e) {}
		}
	}
	
	@Test
	public void testFileEntityRoundTrip() throws SynapseException, IOException, InterruptedException, JSONObjectAdapterException{
		// Before we start the test wait for the preview to be created
		waitForPreviewToBeCreated(fileHandle);
		// Now create a FileEntity
		FileEntity file = new FileEntity();
		file.setName("IT054FileEntityTest.testFileEntityRoundTrip");
		file.setParentId(project.getId());
		file.setDataFileHandleId(fileHandle.getId());
		// Create it
		file = synapse.createEntity(file);
		assertNotNull(file);
		// Get the file handles
		FileHandleResults fhr = synapse.getEntityFileHandlesForCurrentVersion(file.getId());
		assertNotNull(fhr);
		assertNotNull(fhr.getList());
		assertEquals(2, fhr.getList().size());
		assertEquals(fileHandle.getId(), fhr.getList().get(0).getId());
		assertEquals(previewFileHandle.getId(), fhr.getList().get(1).getId());
		// Repeat the test for version
		fhr = synapse.getEntityFileHandlesForVersion(file.getId(), file.getVersionNumber());
		assertNotNull(fhr);
		assertNotNull(fhr.getList());
		assertEquals(2, fhr.getList().size());
		assertEquals(fileHandle.getId(), fhr.getList().get(0).getId());
		assertEquals(previewFileHandle.getId(), fhr.getList().get(1).getId());
		// Make sure we can get the URLs for this file
		String expectedKey = URLEncoder.encode(fileHandle.getKey(), "UTF-8");
		URL tempUrl = synapse.getFileEntityTemporaryUrlForCurrentVersion(file.getId());
		assertNotNull(tempUrl);
		assertTrue("The temporary URL did not contain the expected file handle key",tempUrl.toString().indexOf(expectedKey) > 0);
		// Get the url using the version number
		tempUrl = synapse.getFileEntityTemporaryUrlForVersion(file.getId(), file.getVersionNumber());
		assertNotNull(tempUrl);
		assertTrue("The temporary URL did not contain the expected file handle key",tempUrl.toString().indexOf(expectedKey) > 0);
		// Now get the preview URLs
		String expectedPreviewKey = URLEncoder.encode(previewFileHandle.getKey(), "UTF-8");
		tempUrl = synapse.getFileEntityPreviewTemporaryUrlForCurrentVersion(file.getId());
		assertNotNull(tempUrl);
		assertTrue("The temporary URL did not contain the expected file handle key",tempUrl.toString().indexOf(expectedPreviewKey) > 0);
		// Get the preview using the version number
		tempUrl = synapse.getFileEntityPreviewTemporaryUrlForVersion(file.getId(), file.getVersionNumber());
		assertNotNull(tempUrl);
		assertTrue("The temporary URL did not contain the expected file handle key",tempUrl.toString().indexOf(expectedPreviewKey) > 0);
		System.out.println(tempUrl);
	}
	
	@Test
	public void testPromoteFileEntityVersion() throws Exception {

		// Create a file entity
		FileEntity file = new FileEntity();
		file.setName("IT054FileEntityTest.testPromoteFileEntityVersion");
		file.setParentId(project.getId());
		file.setDataFileHandleId(fileHandle.getId());
		FileEntity file1 = synapse.createEntity(file);
		assertNotNull(file1);
		assertEquals(Long.valueOf(1L), file1.getVersionNumber());

		// Create a new version
		file1.setName("IT054FileEntityTest.testPromoteFileEntityVersion 2");
		file1.setDataFileHandleId(fileHandle2.getId());
		file1.setVersionLabel("Version label 2");
		file1.setVersionComment("Version comment 2");
		FileEntity file2 = synapse.createNewEntityVersion(file1);
		assertNotNull(file2);
		assertEquals(Long.valueOf(2L), file2.getVersionNumber());
		assertEquals(file1.getId(), file2.getId());

		// Promote version 1
		VersionInfo versionInfo = synapse.promoteEntityVersion(file1.getId(), Long.valueOf(1L));
		assertEquals(Long.valueOf(3L), versionInfo.getVersionNumber());

		// Check the file handles
		List<FileHandle> fileHandles1 = synapse.getEntityFileHandlesForVersion(file1.getId(), Long.valueOf(1L)).getList();
		assertNotNull(fileHandles1);
		assertTrue(fileHandles1.size() > 0);
		assertTrue(fileHandles1.get(0) instanceof S3FileHandle);
		S3FileHandle fh1 = (S3FileHandle)fileHandles1.get(0);

		List<FileHandle> fileHandles3 = synapse.getEntityFileHandlesForCurrentVersion(file1.getId()).getList();
		assertNotNull(fileHandles3);
		assertTrue(fileHandles3.size() > 0);
		assertTrue(fileHandles3.get(0) instanceof S3FileHandle);
		S3FileHandle fh3 = (S3FileHandle)fileHandles3.get(0);

		assertEquals(fh1.getId(), fh3.getId());
		assertEquals(fh1.getFileName(), fh3.getFileName());
		assertEquals(fh1.getContentMd5(), fh3.getContentMd5());
	}

	/**
	 * Wait for a preview to be generated for the given file handle.
	 * @throws InterruptedException
	 * @throws SynapseException
	 */
	private void waitForPreviewToBeCreated(S3FileHandle fileHandle) throws InterruptedException,
			SynapseException {
		long start = System.currentTimeMillis();
		while(fileHandle.getPreviewId() == null){
			System.out.println("Waiting for a preview file to be created");
			Thread.sleep(1000);
			assertTrue("Timed out waiting for a preview to be created",(System.currentTimeMillis()-start) < MAX_WAIT_MS);
			fileHandle = (S3FileHandle) synapse.getRawFileHandle(fileHandle.getId());
		}
		// Fetch the preview file handle
		previewFileHandle = (PreviewFileHandle) synapse.getRawFileHandle(fileHandle.getPreviewId());
	}
	
}
