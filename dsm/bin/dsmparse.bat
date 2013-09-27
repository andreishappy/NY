@ECHO OFF 
REM ---------------------------------------------------------------------------
REM Copyright IBM Corporation 2008, 2011.
REM 
REM GOVERNMENT PURPOSE RIGHTS
REM 
REM Contract No. W911NF-06-3-0002
REM Contractor Name: IBM 
REM Contractor Address:  IBM T. J. Watson Research Center.
REM                      19 Skyline Drive
REM                     Hawthorne, NY 10532 
REM
REM The Government's rights to use, modify, reproduce, release, perform, 
REM display or disclose this software are restricted by Article 10 
REM Intellectual Property Rights clause contained in the above 
REM identified contract. Any reproductions of the software or portions 
REM thereof marked with this legend must also reproduce the markings.
REM ---------------------------------------------------------------------------

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal
set DSMCLASS=com.ibm.watson.dsm.engine.parser.dsm.DSMParser

REM Try and set DSM_HOME and PATH automatically assuming this script is being run from DSM_HOME/bin
if NOT [%DSM_HOME%] == [] goto homeset
REM %~dp0 is the directory containing this file (see http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/percent.mspx)
set DSM_HOME=%~dp0..
echo Setting DSM_HOME automatically to %DSM_HOME%
set PATH=%DSM_HOME%\bin;%PATH%
:homeset

REM Do the actual work!
dsmrun %DSMCLASS%  %*

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
