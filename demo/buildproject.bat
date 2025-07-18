@echo off
REM =====================
REM Указываем пути и переменные
REM =====================

set BASE_DIR=C:\Users\1\Documents\Practice-native\demo
set SRC_DIR=%BASE_DIR%\src
set CLASSES_DIR=%BASE_DIR%\out\classes
set JAR_NAME=%BASE_DIR%\test.jar
set MANIFEST_FILE=%BASE_DIR%\manifest.txt
set MAIN_CLASS=com.example.Main

REM Пути к библиотекам Jackson и Gson
set LIBS_PATH=%BASE_DIR%\libs

REM Пути к исходным файлам Java
set JAVA_SRC_MAIN=%SRC_DIR%\main\java\com\example\Main.java
set JAVA_SRC_PARSER=%SRC_DIR%\main\java\com\example\JsonParsery.java
set JAVA_SRC_CONVERTER=%SRC_DIR%\main\java\com\example\DataConverter.java

REM Пути к Native исходникам и DLL
set NATIVE_SRC=%SRC_DIR%\main\native\nativeLib.c
set DLL_NAME=C:\Users\1\Documents\Practice-native\demo\aggregation.dll
set DLL_PATH=%BASE_DIR%

REM Пути к JDK
set JAVA_HOME=C:\Program Files\Java\jdk-24
set JAVA_INCLUDE_PATH=%JAVA_HOME%\include
set JAVA_INCLUDE_WIN_PATH=%JAVA_HOME%\include\win32


REM Путь к заголовочным файлам C++
set CPP_INCLUDE_PATH=C:\msys64\mingw64\include\c++\15.1.0

REM Добавляем JDK и MinGW в PATH
set PATH=C:\msys64\mingw64\bin;%JAVA_HOME%\bin;%PATH%

REM Класспасс для компиляции
set CLASSPATH="%LIBS_PATH%\gson-2.10.1.jar;%LIBS_PATH%\jackson-databind-2.15.2.jar;%LIBS_PATH%\jackson-core-2.15.2.jar;%LIBS_PATH%\jackson-annotations-2.15.2.jar"

REM =====================
REM Удаляем старые файлы и папки
REM =====================

echo Удаление старых файлов и папок...

if exist "%CLASSES_DIR%" (
    rmdir /s /q "%CLASSES_DIR%"
)

if exist "%JAR_NAME%" (
    del /f /q "%JAR_NAME%"
)

if exist "%DLL_PATH%%DLL_NAME%" (
    del /f /q "%DLL_PATH%%DLL_NAME%"
)

REM Создаем необходимые папки
mkdir "%CLASSES_DIR%"

REM =====================
REM Компилируем Java исходники
REM =====================

echo Компиляция Java...

javac -d "%CLASSES_DIR%" -classpath "%CLASSPATH%;%SRC_DIR%\main\java" "%JAVA_SRC_MAIN%" "%JAVA_SRC_PARSER%" "%JAVA_SRC_CONVERTER%"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Java.
    goto end
)

REM =====================
REM Генерируем JNI заголовочный файл
REM =====================

echo Генерация JNI заголовочного файла...
javac -h "%SRC_DIR%\main\native" -classpath "%CLASSPATH%" "%JAVA_SRC_MAIN%" "%JAVA_SRC_PARSER%"
if %errorlevel% neq 0 (
    echo Ошибка при генерации JNI заголовочного файла.
    goto end
)

REM =====================
REM Компилируем Native код в DLL
REM =====================

echo Компиляция Native кода...
gcc -c  -O3 -I"%JAVA_INCLUDE_PATH%" -I"%JAVA_INCLUDE_WIN_PATH%" -I"C:\mingw64\include" -o nativeLib.o "%NATIVE_SRC%"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Native кода в объектный файл.
    goto end
)

x86_64-w64-mingw32-g++ -shared -m64 -o aggregation.dll nativeLib.o --verbose
if %errorlevel% neq 0 (
    echo Ошибка при компоновке Native кода в DLL.
    goto end
)

REM =====================
REM Обновляем manifest.txt
REM =====================

echo Обновление manifest.txt...
(
echo Manifest-Version: 1.0
echo Main-Class: %MAIN_CLASS%
echo Class-Path: libs/gson-2.10.1.jar libs/jackson-databind-2.15.2.jar libs/jackson-core-2.15.2.jar libs/jackson-annotations-2.15.2.jar
) > "%MANIFEST_FILE%"

REM =====================
REM Создаем JAR файл
REM =====================

echo Создание JAR файла...
jar cfm "%JAR_NAME%" "%MANIFEST_FILE%" -C "%CLASSES_DIR%" .
if %errorlevel% neq 0 (
    echo Ошибка при создании JAR.
    goto end
)

echo Процесс завершен успешно!

REM =====================
REM Запуск JAR файла
REM =====================

echo Запуск JAR файла...
java --enable-native-access=ALL-UNNAMED  -jar test.jar -a max -f number -g name -d "C:\Users\1\Documents\Practice-native\nabor(group).json"
if %errorlevel% neq 0 (
    echo Ошибка при запуске JAR файла.
    goto end
)

:end
pause