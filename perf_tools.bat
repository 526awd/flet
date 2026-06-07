@echo off
setlocal
echo ==========================================
echo    Flet Performance Optimization Script
echo ==========================================

:MENU
echo 1. Generate Baseline Profile (Boost Startup)
echo 2. Run Startup Benchmarks
echo 3. Build Optimized Release APK
echo 4. Run Lint Analysis
echo 5. Clean Project
echo 6. Exit
set /p opt="Select an option (1-6): "

if "%opt%"=="1" goto GEN_PROFILE
if "%opt%"=="2" goto RUN_BENCH
if "%opt%"=="3" goto BUILD_RELEASE
if "%opt%"=="4" goto RUN_LINT
if "%opt%"=="5" goto CLEAN
if "%opt%"=="6" goto :EOF

:GEN_PROFILE
echo [Task] Generating Baseline Profile...
call gradlew :app:generateReleaseBaselineProfile
echo [Done] Profile generated and applied to the app.
pause
goto MENU

:RUN_BENCH
echo [Task] Running Startup Benchmarks...
call gradlew :up:connectedReleaseAndroidTest
echo [Done] Check build/reports/androidTests/connected for results.
pause
goto MENU

:BUILD_RELEASE
echo [Task] Building Minified Release APK...
call gradlew :app:assembleRelease
echo [Done] APK located in app/build/outputs/apk/release/
pause
goto MENU

:RUN_LINT
echo [Task] Running Android Lint...
call gradlew :app:lintRelease
echo [Done] Report at app/build/reports/lint-results-release.html
pause
goto MENU

:CLEAN
echo [Task] Cleaning...
call gradlew clean
echo [Done]
pause
goto MENU
