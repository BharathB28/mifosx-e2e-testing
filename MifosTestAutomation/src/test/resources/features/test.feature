Feature:RBI

Background:
	Given I navigate to mifos using "default7#/"
	And I login into mifos site using "Login" excel sheet
		|Login.xlsx|
	Then I should see logged in successfully

@RunnerClass4
Scenario:4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN 
	 Given I setup the center
	 When I entered the values into center from "Input" sheet
	 |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|
	 Then I entered the values into group from "Group" sheet
     |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|		  								  				  				  			
	 Then I entered the values into client from "Input" sheet
	 	|Createclient.xlsx|	 				  								  				  				  			
	 When I set up the new create loan from "NewLoanInput" sheet
	 |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|
     Then I "WAIVEPENALTY" and verified the following tabs
	 |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|Charges|
	 Then I navigate to collection Sheet
     Then I Make Repayment Through "Productive Collection" sheet
	 |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|
	 And I navigate To Loan Account Page
	 Then I verified the following Tabs details successfully 
	 |4566-MS-ACT2CTR-MEET-WEEKLYonFRI-ASSIGNSTAFF-ACTGRP-ACTCLIENT-DISJLG02JAN-WAIVECHARGEFOR1LOAN-PRODUCTIVE_COLLSHEET_ONTIME09JAN-C1.xlsx|Summary|Repayment Schedule|
			