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

REM This is how to get the output of a command into a variable. 
REM Equivalent in shell is XXX_CP=`dsmjars`
for /F %%X in ('dsmjars.bat') do SET XXX_CP=%%X
set CLASSPATH=%XXX_CP%;%CLASSPATH%

REM Separate out the "-Dfoo=bar" parameters to make sure they go at the beginning.
:checkparams
REM Remove "" from argument, which must include them otherwise DOS splits on the '=' sign.
set PARAM=%~1
if "%PARAM%" == "" goto endcheckparams
if "%PARAM:~0,2%" == "-D" goto DO_D_PARAM
set NON_D_PARAMS=%NON_D_PARAMS% %PARAM%
SHIFT
goto checkparams
:DO_D_PARAM
set D_PARAMS=%D_PARAMS% %PARAM%
SHIFT
goto checkparams
:endcheckparams


set WPML_HOME=%DSM_HOME%
REM Do the actual work!
REM @echo ON
java %D_PARAMS% -Djava.library.path=%DSM_HOME%\lib %NON_D_PARAMS%
REM java  -Djava.library.path=c:\dev\concert-ws\generic\DSM-Core\lib %*

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
