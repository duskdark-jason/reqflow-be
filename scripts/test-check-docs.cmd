@echo off
setlocal

set "BASH_EXE="

if exist "%ProgramFiles%\Git\bin\bash.exe" set "BASH_EXE=%ProgramFiles%\Git\bin\bash.exe"
if not defined BASH_EXE if exist "%LocalAppData%\Programs\Git\bin\bash.exe" set "BASH_EXE=%LocalAppData%\Programs\Git\bin\bash.exe"
if not defined BASH_EXE if exist "%ProgramFiles(x86)%\Git\bin\bash.exe" set "BASH_EXE=%ProgramFiles(x86)%\Git\bin\bash.exe"
if not defined BASH_EXE (
  for /f "delims=" %%B in ('where bash 2^>nul') do (
    if not defined BASH_EXE set "BASH_EXE=%%B"
  )
)

if not defined BASH_EXE (
  echo Git Bash is required to run test-check-docs.cmd from Windows cmd.
  echo WSL users should enter the repository in a WSL shell and run: sh scripts/test-check-docs.sh
  exit /b 1
)

"%BASH_EXE%" "%~dp0test-check-docs.sh" %*
exit /b %ERRORLEVEL%
