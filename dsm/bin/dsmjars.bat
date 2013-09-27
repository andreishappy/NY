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

if "%1" == "help" goto do_help:
if "%1" == "-help" goto do_help:
if "%1" == "--help" goto do_help:

if "%DSM_HOME%" == "" goto no_home_error 

REM Setting the class path to include wmpl.jar is sufficient if the
REM other jars are in the same directory.

SETLOCAL ENABLEDELAYEDEXPANSION
set CP=
set DIRS=%DSM_HOME%\lib %*
if exist %DSM_HOME%\lib-int SET DIRS=%DIRS% %DSM_HOME%\lib-int
for %%D in (%DIRS%) DO CALL :GETJARS %%D
echo %CP%
goto end 

:GETJARS
PUSHD %*
REM for /R %%J in (*.jar) DO SET CP=!CP!;%%J
for /R %%J in (*.jar) DO CALL :APPEND_CP %%J 
POPD
GOTO :EOF

:APPEND_CP
if NOT "%CP%" == "" SET CP=%CP%;
set CP=%CP%%*
GOTO :EOF

:do_help
    echo 
    echo Usage:
    echo Retrieves the list of jars in a colon-separated list suitable
    echo for setting the Java CLASS_PATH. It always returns the list
    echo of jars in DSM_HOME/lib, but additional directories may be
    echo included on the command line.
    echo Examples:
    echo   %0 #retrieves the jars in DSM_HOME/lib
    echo   %0 /homes/me/java/lib # gets jars in DSM_HOME/lib
    echo        and the given directory.

goto end


:no_home_error
echo ERROR: DSM_HOME environment variable must be set.

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal


