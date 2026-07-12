@echo off
title Volcan-Engine Release Compiler V1.0
cls

echo [SYSTEM] Starting Volcan-Engine Production Build V1.0
echo ---------------------------------------------------

:: 1. Clean
if exist bin rd /s /q bin
if exist Volcan-Engine-V1.0 rd /s /q Volcan-Engine-V1.0
if exist VolcanEngine-v1.0.jar del /q VolcanEngine-v1.0.jar
mkdir bin

:: Detect javac version
for /f "tokens=2 delims= " %%v in ('javac --version 2^>^&1') do set JAVAC_VER=%%v
for /f "tokens=1 delims=." %%m in ("%JAVAC_VER%") do set JAVA_MAJOR=%%m
echo [INFO] Compiler: JDK %JAVAC_VER% (Aggressive Zero-Debug Mode)

:: 2. Discover Source Files (excluding tests)
echo [STAGE] Discovering source files (Excluding test packages)...
dir /s /B src\*.java | findstr /v "\\test\\" | findstr /v "\\benchmark\\" > compile_release_list.txt

:: 3. Compile
echo [STAGE] Compiling Source without debug symbols (-g:none)...
javac -d bin -g:none --enable-preview --source %JAVA_MAJOR% ^
      --add-modules jdk.incubator.vector,jdk.httpserver ^
      -Xlint:-incubating ^
      -cp "src" ^
      -J-XX:+UseZGC -J-Xms4G -J-Xmx4G -J-XX:+AlwaysPreTouch ^
      @compile_release_list.txt

if %errorlevel% neq 0 (
    echo [ERROR] Production Compilation failed.
    exit /b %errorlevel%
)

:: 4. Inject Server Resources
echo [STAGE] Injecting server static resources...
if not exist bin\sv\volcan\ui mkdir bin\sv\volcan\ui
copy /y src\sv\volcan\ui\volcanengine_logo.png bin\sv\volcan\ui\volcanengine_logo.png >nul

:: 5. Package as executable JAR
cd bin
if exist META-INF rd /s /q META-INF
cd ..
echo [STAGE] Creating VolcanEngine-v1.0.jar...
jar --create --file VolcanEngine-v1.0.jar --main-class sv.volcan.state.VolcanEngineMaster -C bin .

if %errorlevel% neq 0 (
    echo [ERROR] JAR creation failed.
    exit /b %errorlevel%
)

:: Isolate JAR for JPackage
if exist release_input rd /s /q release_input
mkdir release_input
move VolcanEngine-v1.0.jar release_input\ >nul

:: 6. Generate Native App Image with JPackage
echo [STAGE] Generating Native Executable Image (JPackage)...
jpackage --name "Volcan-Engine" ^
         --input release_input ^
         --main-jar VolcanEngine-v1.0.jar ^
         --type app-image ^
         --dest Volcan-Engine-V1.0 ^
         --win-console ^
         --add-modules jdk.incubator.vector,java.base,jdk.httpserver,jdk.management,java.desktop,jdk.unsupported ^
         --java-options "--enable-preview --enable-native-access=ALL-UNNAMED -XX:+UseZGC -Xms4G -Xmx4G -XX:+AlwaysPreTouch --add-modules jdk.incubator.vector"

if %errorlevel% neq 0 (
    echo [ERROR] jpackage failed.
    exit /b %errorlevel%
)

:: 7. Complete
if exist compile_release_list.txt del /q compile_release_list.txt

echo ---------------------------------------------------
echo [SUCCESS] Volcan-Engine V1.0 Packaging Complete!
echo [READY] Run: Volcan-Engine-V1.0\Volcan-Engine\Volcan-Engine.exe
echo ---------------------------------------------------
