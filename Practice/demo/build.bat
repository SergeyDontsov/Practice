@echo off
setlocal

:: Пути и переменные
set BASE_DIR=C:\Users\1\Documents\Practice\demo
set SRC_DIR=%BASE_DIR%\src
set OUT_DIR=%BASE_DIR%\out
set CLASSES_DIR=%OUT_DIR%\classes
set JAR_FILE=%BASE_DIR%\test.jar
set MANIFEST_FILE=%BASE_DIR%\manifest.txt

:: Пути к JDK
set JAVA_HOME=C:\Program Files\Java\jdk-24
set JAVA_INCLUDE=%JAVA_HOME%\include
set JAVA_INCLUDE_WIN=%JAVA_HOME%\include\win32

:: Пути к зависимостям (JAR-файлы)
set LIBS_DIR=%BASE_DIR%\libs
set CLASSPATH=%LIBS_DIR%\jackson-annotations-2.15.2.jar;%LIBS_DIR%\jackson-core-2.15.2.jar;%LIBS_DIR%\jackson-databind-2.15.2.jar

:: Очистка старых файлов
echo Удаление старых файлов...
if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"
if exist "%JAR_FILE%" del /f /q "%JAR_FILE%"
if exist "%MANIFEST_FILE%" del /f /q "%MANIFEST_FILE%"

:: Создание папки для классов
mkdir "%CLASSES_DIR%"

:: Компиляция Java файлов
echo Компиляция Java файлов...

echo Компиляция Aggregator.java...
javac -cp "%CLASSPATH%" -d "%CLASSES_DIR%" "%SRC_DIR%\main\java\com\example\Aggregator.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Aggregator.java
    goto end
)

echo Компиляция CommandLineArgs.java...
javac -cp "%CLASSPATH%" -d "%CLASSES_DIR%" "%SRC_DIR%\main\java\com\example\CommandLineArgs.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции CommandLineArgs.java
    goto end
)

echo Компиляция DataLoader.java...
javac -cp "%CLASSPATH%" -d "%CLASSES_DIR%" "%SRC_DIR%\main\java\com\example\DataLoader.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции DataLoader.java
    goto end
)


echo Компиляция Profiler.java...
javac -cp "%CLASSPATH%" -d "%CLASSES_DIR%" "%SRC_DIR%\main\java\com\example\Profiler.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Profiler.java
    goto end
)

echo Компиляция Main.java...
javac -cp "%CLASSPATH%;%CLASSES_DIR%" -d "%CLASSES_DIR%" "C:\Users\1\Documents\Practice\demo\src\main\java\com\example\Main.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Main.java
    goto end
)


if %ERRORLEVEL% NEQ 0 (
    echo Ошибка при компиляции Java.
    goto end
)

:: Создание manifest.txt
echo Создание manifest.txt...
(
echo Manifest-Version: 1.0
echo Main-Class: com.example.Main
echo Class-Path: libs/jackson-annotations-2.15.2.jar libs/jackson-core-2.15.2.jar libs/jackson-databind-2.15.2.jar
) > "%MANIFEST_FILE%"

:: Создание JAR файла
echo Создание JAR файла...
jar cfm "%JAR_FILE%" "%MANIFEST_FILE%" -C "%CLASSES_DIR%" .

if %ERRORLEVEL% NEQ 0 (
    echo Ошибка при создании JAR.
    goto end
)

:: Запуск приложения
echo Запуск приложения...
java -cp "%JAR_FILE%;%LIBS_DIR%\*" --enable-native-access=ALL-UNNAMED -jar "%JAR_FILE%" -a max -f number -g name -d "C:\Users\1\Documents\Practice\nabor(group).json"

:end
pause