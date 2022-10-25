@echo off
REM ############################################################
REM ## SAP JCo EDRM Install Script
REM ## OS: Windows; PC: CE-Policy Controller
REM ############################################################
SETLOCAL ENABLEDELAYEDEXPANSION

REM #### Identify 64/32bit ###
IF "%~1"=="" (SET Computer=%ComputerName%) ELSE (SET Computer=%~1)
IF /I NOT "%Computer%"=="%ComputerName%" (
	PING %Computer% -n 2 2>NUL | FIND "TTL=" >NUL
)
FOR /F "tokens=2 delims==" %%A IN ('WMIC /Node:"%Computer%" Path Win32_Processor Get AddressWidth /Format:list') DO SET OSB=%%A

REM #### Default values
SET NXL_PC_HOME=C:\Program Files\NextLabs\Policy Controller\
SET RMAPI_HANDLER=\NXLEDRM\IRM_API_AGENT
SET SERVER_PREFIX=SERVRMI_
SET KEY_STORE_NAME=C:\Program Files\NextLabs\Policy Controller\jservice\jar\keymanagement\rmskmc-keystore.jks
SET KEY_STORE_PASSWORD=sa1f78f49e437288039751654ece96ede
SET TRUST_STORE_NAME=C:\Program Files\NextLabs\Policy Controller\jservice\jar\keymanagement\rmskmc-truststore.jks
SET TRUST_STORE_PASSWORD=sa1f78f49e437288039751654ece96ede
SET PC_HOST_NAME=localhost
SET RMI_PORT_NUM=1499

:ShowMainOptions
cls
echo ######### SAP JCo EDRM Deployment Manager ###########
echo.
echo NOTE: Please STOP Policy Controller before proceeding further.
echo.
echo    [1] Set root directory of Policy Controller [!NXL_PC_HOME!]. 
echo    [8] Install
echo    [9] Uninstall
echo    [0] Exit
echo.
GOTO ChooseMainOption

:ChooseMainOption
set CO=0
set /P CO=Choose an option [default is !CO!] :
IF  %CO% NEQ 1 IF %CO% NEQ 8 IF %CO% NEQ 9 IF %CO% NEQ 0 GOTO ShowMainOptions
set MO=MainOption!CO!
GOTO %MO%

:MainOption1
set /P NXL_PC_HOME=Enter root directory of PC[default is !NXL_PC_HOME!] :
set NXL_PC_HOME=!NXL_PC_HOME:"=!
GOTO ShowMainOptions

:MainOption8
cls
echo ######### SAP JCo EDRM Deployment Manager ###########
echo.
echo Installation Menu
echo.
echo    [1] Set Handler [!RMAPI_HANDLER!]
echo    [2] Set Server Prefix. [!SERVER_PREFIX!] 
echo    [3] Set Key Store Name [!KEY_STORE_NAME!]
echo    [4] Set Key Store Password [!KEY_STORE_PASSWORD!]
echo    [5] Set Trust Store Name [!TRUST_STORE_NAME!]
echo    [6] Set Trust Store Password [!TRUST_STORE_PASSWORD!]
echo    [7] Set RMI Port Number [!RMI_PORT_NUM!]
echo    [9] Proceed with INSTALLATION. Make sure all above values are correctly set.
echo    [0] Back
echo.
GOTO ChooseInsOption

:MainOption9
cls
echo ######### SAP JCo EDRM Deployment Manager ###########
echo.
echo Uninstallation Menu
echo.
echo    [9] Proceed with UNINSTALLATION. All SAP JCo EDRM binaries and SAPJCo-EDRM.properties will be deleted.
echo    [0] Back
echo.
GOTO ChooseUninsOption

:MainOption0
GOTO FinalExit

:FinalExit
set /p DUMMY=Hit ENTER to complete.
EXIT /B

:ChooseInsOption
set CO=0
set /P CO=Choose an option [default is !CO!] :
IF  %CO% NEQ 0 IF %CO% NEQ 1 IF %CO% NEQ 2 IF %CO% NEQ 3 IF %CO% NEQ 4 IF %CO% NEQ 5 IF %CO% NEQ 6 IF %CO% NEQ 7 IF %CO% NEQ 8 IF %CO% NEQ 9 GOTO MainOption8
set IO=InsOption!CO!
GOTO %IO%

:InsOption0
GOTO ShowMainOptions

:InsOption1
set /P RMAPI_HANDLER=Enter Handler[default is !RMAPI_HANDLER!] :
GOTO MainOption8

:InsOption2
set /P SERVER_PREFIX=Enter Server Prefix[default is !SERVER_PREFIX!] :
GOTO MainOption8

:InsOption3
set /P KEY_STORE_NAME=Enter Key Store Name[default is !KEY_STORE_NAME!] :
GOTO MainOption8

:InsOption4
set /P KEY_STORE_PASSWORD=Enter Key Store Password[default is !KEY_STORE_PASSWORD!] :
GOTO MainOption8

:InsOption5
set /P TRUST_STORE_NAME=Enter Trust Store Name[default is !TRUST_STORE_NAME!] :
GOTO MainOption8

:InsOption6
set /P TRUST_STORE_PASSWORD=Enter Trust Store Password[default is !KEY_STORE_PASSWORD!] :
GOTO MainOption8

:InsOption7
set /P RMI_PORT_NUM=Enter RMI Port Number[default is !RMI_PORT_NUM!] :
GOTO MainOption8

:InsOption9
echo Installing SAP JCo EDRM ...
echo #Auto Generated>.\config\temp.properties
FOR /F "tokens=* delims=" %%x in (.\config\SAPJCo-EDRM.properties) DO (
	set FILE_CONTENT=%%x
	set FILE_CONTENT=!FILE_CONTENT:/NXLEDRM/IRM_API_AGENT=%RMAPI_HANDLER%!
	set FILE_CONTENT=!FILE_CONTENT:SERVRMI_=%SERVER_PREFIX%!
	set FILE_CONTENT=!FILE_CONTENT:$KEY_STORE_NAME$=%KEY_STORE_NAME%!
	set FILE_CONTENT=!FILE_CONTENT:$KEY_STORE_PASSWORD$=%KEY_STORE_PASSWORD%!
	set FILE_CONTENT=!FILE_CONTENT:$TRUST_STORE_NAME$=%TRUST_STORE_NAME%!
	set FILE_CONTENT=!FILE_CONTENT:$TRUST_STORE_PASSWORD$=%TRUST_STORE_PASSWORD%!
	set FILE_CONTENT=!FILE_CONTENT:$RMI_PORT_NUM$=%RMI_PORT_NUM%!
	echo !FILE_CONTENT!>>.\config\temp.properties
)
REM #### Create necessary folders ###
IF NOT EXIST "%NXL_PC_HOME%\jservice\jar\sap\" MKDIR "%NXL_PC_HOME%\jservice\jar\sap\"
IF NOT EXIST "%NXL_PC_HOME%\jservice\config\" MKDIR "%NXL_PC_HOME%\jservice\config\"
IF NOT EXIST "%NXL_PC_HOME%\jre\lib\ext\" MKDIR "%NXL_PC_HOME%\jre\lib\ext\"
REM #### Copy files ###
COPY .\config\temp.properties "%NXL_PC_HOME%\jservice\config\SAPJCo-EDRM.properties" >NUL
DEL  .\config\temp.properties >NUL
COPY .\jars\SAPJCo-EDRM.jar "%NXL_PC_HOME%\jservice\jar\sap\" >NUL
COPY .\xlib\jar\*.jar "%NXL_PC_HOME%\jre\lib\ext\" >NUL
echo DONE.
GOTO FinalExit

:ChooseUninsOption
set CO=0
set /P CO=Choose an option [default is !CO!] :
IF  %CO% NEQ 0 IF %CO% NEQ 9 GOTO MainOption9
set UO=UninsOption!CO!
GOTO %UO%

:UninsOption0
GOTO ShowMainOptions

:UninsOption9
echo Uninstalling SAP JCo ...
REM #### Delete files ###
DEL /Q "%NXL_PC_HOME%\jservice\config\SAPJCo-EDRM.properties" 2>NUL
DEL /Q "%NXL_PC_HOME%\jservice\jar\sap\SAPJCo-EDRM.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\commons-io-2.4.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\dom4j-1.6.1.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\nextlabs-jtagger.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\pdfbox-app-1.8.8.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\poi-3.11-20141221.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\poi-ooxml-3.11-20141221.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\poi-ooxml-schemas-3.11-20141221.jar" 2>NUL
DEL /Q "%NXL_PC_HOME%\jre\lib\ext\xmlbeans-2.6.0.jar" 2>NUL
echo DONE.
GOTO FinalExit