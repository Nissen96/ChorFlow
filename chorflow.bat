@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  ChorFlow startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%.

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and CHOR_FLOW_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ChorFlow-1.0.jar;%APP_HOME%\lib\antlr4-4.11.1.jar;%APP_HOME%\lib\graphviz-kotlin-0.18.1.jar;%APP_HOME%\lib\clikt-jvm-3.5.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.7.10.jar;%APP_HOME%\lib\log4j-slf4j-impl-2.19.0.jar;%APP_HOME%\lib\j2v8_win32_x86_64-4.6.0.jar;%APP_HOME%\lib\antlr4-runtime-4.11.1.jar;%APP_HOME%\lib\ST4-4.3.4.jar;%APP_HOME%\lib\antlr-runtime-3.5.3.jar;%APP_HOME%\lib\org.abego.treelayout.core-1.0.3.jar;%APP_HOME%\lib\javax.json-1.1.4.jar;%APP_HOME%\lib\icu4j-71.1.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.7.10.jar;%APP_HOME%\lib\kotlin-stdlib-1.7.10.jar;%APP_HOME%\lib\graphviz-java-0.18.1.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.30.jar;%APP_HOME%\lib\jul-to-slf4j-1.7.30.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\log4j-core-2.19.0.jar;%APP_HOME%\lib\log4j-api-2.19.0.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.7.10.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\viz.js-graphviz-java-2.1.3.jar;%APP_HOME%\lib\svgSalamander-1.1.3.jar;%APP_HOME%\lib\nashorn-promise-0.1.1.jar;%APP_HOME%\lib\commons-exec-1.3.jar;%APP_HOME%\lib\jsr305-3.0.2.jar


@rem Execute ChorFlow
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CHOR_FLOW_OPTS%  -classpath "%CLASSPATH%" chorflow.MainKt %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable CHOR_FLOW_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%CHOR_FLOW_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
