@echo off
echo Lancement du backend Spring Boot...
echo.

REM Vérifier Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java 17+ et configurer JAVA_HOME.
    pause
    exit /b 1
)

echo Version Java:
java -version
echo.

echo Demarrage du serveur Spring Boot...
echo Le serveur sera accessible sur http://localhost:8080/api
echo Appuyez sur Ctrl+C pour arreter le serveur
echo.

call mvnw.cmd spring-boot:run

pause
