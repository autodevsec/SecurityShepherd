package dbProcs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.junit.Test;

public class GetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GetterTest.class);
	private static String propertiesFileDirectory = new String("/site");
	private static String unOpenedModuleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
	private static Locale lang = new Locale("en");
	private static String applicationRoot = new String();
	private static String adminUserName = new String("admin");
	private static String adminUserId = new String();
	
	public GetterTest()
	{
		applicationRoot = System.getProperty("user.dir") + propertiesFileDirectory;
		adminUserId = Getter.getUserIdFromName(applicationRoot, adminUserName);
	}
	
	@Test
	public void testAuthUserCorrectCredentials() 
	{
		if(Getter.authUser(applicationRoot, adminUserName, "password") == null)
			fail("Could not Authenticate Default admin");
		else
			return; //Pass
	}
	
	@Test
	public void testAuthUserIncorrectCredentials() 
	{
		if(Getter.authUser(applicationRoot, adminUserName, "wrongPassword") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with incorrect Password");
	}
	
	@Test
	public void testAuthUserSqlInjection() 
	{
		if(Getter.authUser(applicationRoot, adminUserName, "wrongPassword'or'1'='1") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with SQL Injection");
	}
	
	@Test
	public void testAuthUserSqlInjectionUserName() 
	{
		if(Getter.authUser(applicationRoot, adminUserName + "'or'1'='1", "wrongPassword") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with SQL Injection (UserName)");
	}

	@Test
	public void testCheckPlayerResultWhenModuleNotOpened() {
		String test = Getter.checkPlayerResult(applicationRoot, unOpenedModuleId, adminUserId);
		if(test != null)
		{
			log.fatal("result should be null but it was: " + test);
			fail("Function says Admin has opened module they should not have opened by default"); // Admin Should not have completed this module by default after running a fresh DB. ensure you have a fresh DB if this fails
		}
		else
			return; //Pass
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleWhenOpened() {
		String csrfChallengeThree = new String("5ca9115f3279b9b9f3308eb6a59a4fcd374846d6");
		
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, csrfChallengeThree, adminUserId).isEmpty())
		{
			String test = Getter.checkPlayerResult(applicationRoot, csrfChallengeThree, adminUserId);
			if(test == null)
			{
				fail("Function says Admin has not opened module"); // Admin Should have opened and not completed CSRF Three. Ensure DB is clean
			}
			else
				return; //Pass
		}
		else
			fail("Could not Mark CSRF 3 as Opened by Default admin");
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleNotComplete() 
	{
		String contentProviderLeakage = new String("5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a");
		
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, contentProviderLeakage, adminUserId).isEmpty())
		{
				String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, contentProviderLeakage, adminUserId);
				if(checkPlayerResultTest != null)
					return; //Pass
				else
				{
					fail("Function says Admin has not opened challenge or has completed challenge before");
				}
		}
		else
			fail("Could not Mark Data Storage Lesson as Opened by Default admin");
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleComplete() 
	{
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, dataStorageLessonId, adminUserId).isEmpty())
		{
			//Then, Mark the Challenge Complete for Default Admin (Insecure Data Storage Lesson)
			String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, dataStorageLessonId, adminUserId, "Feedback is Disabled", 1, 1, 1);
			if (markLevelCompleteTest != null)
			{
				String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, dataStorageLessonId, adminUserId);
				log.debug("checkPlayerResultTest" + checkPlayerResultTest);
				if(checkPlayerResultTest == null)
					return; //Pass
				else
				{
					fail("Function says Admin has not completed module"); //Even though this test just marked it as Completed
				}
			}
			else
				fail("Could not mark data storage lesson as complete Default admin");
		}
		else
			fail("Could not Mark Data Storage Lesson as Opened by Default admin");
	}
	
	
	@Test
	public void testIsCsrfLevelCompleteIncrementedCounter() 
	{
		String csrfChallengeOne = new String("20e755179a5840be5503d42bb3711716235005ea"); //CSRF Challenge 1 (Should have CSRF Counter of 0 for Default Admin User
		
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, csrfChallengeOne, adminUserId).isEmpty())
		{
			//Increment Challenge CSRF Counter
			if(Setter.updateCsrfCounter(applicationRoot, csrfChallengeOne, adminUserId))
			{
				if(Getter.isCsrfLevelComplete(applicationRoot, csrfChallengeOne, adminUserId))
				{
					return; //Pass, because CSRF level is completed after the admin CSRF counter was incremented
				}
				else
				{
					fail("CSRF 1 not completed after successful increment");
				}
			}
			else
			{
				fail("Could not Increment default Admin Counter for CSRF 1");
			}
		}
		else
		{
			fail("Could not Mark CSRF 1 as opened by default Admin");
		}
	}
	
	@Test
	public void testIsCsrfLevelCompleteWithoutIncrementedCounter() 
	{
		String csrfChallengeTwo = new String("94cd2de560d89ef59fc450ecc647ff4d4a55c15d"); //CSRF Challenge 2 (Should have CSRF Counter of 0 for Default Admin User
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, csrfChallengeTwo, adminUserId).isEmpty())
		{
			if(!Getter.isCsrfLevelComplete(applicationRoot, csrfChallengeTwo, adminUserId))
			{
				return; //Pass, because CSRF level is not completed because the CSRF Counter for the default admin is 0
			}
			else
			{
				fail("CSRF 2 marked completed without increment"); // CSRF 2 Challenge should have a counter of 0 and should not return true. 
			}
		}
		else
		{
			fail("Could not Mark CSRF 2 as opened by default Admin");
		}
	}
	
	@Test
	public void testFindPlayerById() 
	{ 
		String userName = "UserForPlayerIdSearch";
		try
		{
			//Create player with Null Class Id, and userName for name and password
			Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
			String userId = Getter.getUserIdFromName(applicationRoot, userName);
			if(Getter.findPlayerById(applicationRoot, userId))
			{
				return;
			}
			else
			{
				fail("Could Not Find Player in Player Search");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not create player: " + e.toString());
			fail("Could Not Create Player");
		}
	}
	
	@Test
	public void testFindPlayerByIdWithAdminId() 
	{ 
		if(!Getter.findPlayerById(applicationRoot, adminUserId))
		{
			return;
		}
		else
		{
			fail("Found Admin in Player Search");
		}
	}
	
	@Test
	public void testFindPlayerByIdWithBadUserId() 
	{
		String userId = new String("DOES NOT EXIST");
		if(!Getter.findPlayerById(applicationRoot, userId))
		{
			return;
		}
		else
		{
			fail("Found Player That Does Not Exist");
		}
	}

	
	@Test
	public void testGetAllModuleInfoWhenModulesOpen() 
	{
		ArrayList<String[]> modules = Getter.getAllModuleInfo(applicationRoot);
		if(modules.size() > 75) //Shepherd v3.0 has 76 Modules. If less than All are Returned, then there is a problem with the Open Modules Function or the Retrieve data function
		{
			log.debug("PASS: Found " + modules.size() + " modules");
			return;
		}
		else
		{
			log.fatal("Too Few Modules Returned to Pass Test: " + modules.size());
			fail("Only " + modules.size() + "/~76 modules returned from function");
		}
	}
	
	
	@Test
	public void testGetChallenges() 
	{
		//Open all Modules First so that the GetAllModuleInfo method will return data
		if(Setter.openAllModules(applicationRoot))
		{
			String modules = Getter.getChallenges(applicationRoot, adminUserId, lang);
			if(!modules.isEmpty()) //Some Modules were included in response
			{
				//Get number of Challenges returned by getChallenges method
				int numberofChallengesReturned = (modules.length() - modules.replace("class='lesson'", "").length()) / "class='lesson'".length();
				if(numberofChallengesReturned > 58)
				{
					log.debug("PASS: Found " + numberofChallengesReturned + " modules");
					return;
				}
				else
				{
					log.debug("Too Few Challenges Returned to pass: " + numberofChallengesReturned + " returned");
					fail("Too Few Challenges Returned to Pass");
				}
			}
			else
			{
				log.fatal("No Modules Found. Returned empty String");
				fail("No Modules Found");
			}
		}
		else
		{
			fail("Could Not Mark Modules as Open Before Test");
		}
	}
	
	@Test
	public void testGetChallengesWhenModulesClosed() {
		//Open all Modules First so that the GetAllModuleInfo method will return data
		if(Setter.closeAllModules(applicationRoot))
		{
			String modules = Getter.getChallenges(applicationRoot, adminUserId, lang);
			if(!modules.isEmpty()) //Some Modules were included in response
			{
				//Get number of Challenges returned by getChallenges method
				int numberofChallengesReturned = (modules.length() - modules.replace("class='lesson'", "").length()) / "class='lesson'".length();
				if(!(numberofChallengesReturned > 0))
				{
					log.debug("PASS: Found " + numberofChallengesReturned + " modules");
					return;
				}
				else
				{
					log.debug("Too Many Challenges Returned to pass: " + numberofChallengesReturned + " returned");
					fail("Challenges Returned when all modules were closed");
				}
			}
			else
			{
				log.fatal("No Modules Found. Returned empty String");
				fail("No Modules Found");
			}
		}
		else
		{
			fail("Could Not Mark Modules as Open Before Test");
		}
	}
	
	@Test
	public void testGetClassCount() {
		if(Setter.classCreate(applicationRoot, "NewClassForGetCount", "2015"))
		{
			int classCount = Getter.getClassCount(applicationRoot);
			if(classCount < 1)
			{
				fail("Class Count Too Low to Pass");
			}
			else
			{
				log.debug("PASS: Atleast One Class Returned");
				return;
			}
		}
		else
		{
			fail("Could not Create Class");
		}
	}
	
	/*
	@Test
	public void testGetClassInfoString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClassInfoStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCsrfForumWithIframe() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCsrfForumWithImg() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFeedback() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIncrementalModules() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIncrementalModulesWithoutScript() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetJsonScore() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLessons() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleAddress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleCategory() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleIdFromHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleKeyType() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleResult() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleResultFromHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModulesInOptionTags() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModulesInOptionTagsCTF() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleSolution() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleStatusMenu() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOpenCloseCategoryMenu() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlayersByClass() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgressJSON() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTournamentModules() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserName() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUserLocked() {
		fail("Not yet implemented");
	}
	*/

}
