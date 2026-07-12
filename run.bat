@echo off
setlocal

set JVM_OPTS=--enable-preview --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -XX:+UseZGC -Xms1G -Xmx1G

echo [EXEC] Launching VolcanEngine...
java %JVM_OPTS% -cp "bin" sv.volcan.state.VolcanEngineMaster

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Engine crashed or failed to launch! Check the console output above.
    pause
) else (
    echo [EXEC] VolcanEngine closed cleanly.
)

endlocal
