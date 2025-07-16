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

REM Пути к библиотекам
set LIBS_PATH=%BASE_DIR%\libs

REM Пути к исходным файлам Java
set JAVA_SRC_MAIN=%SRC_DIR%\main\java\com\example\Main.java
set JAVA_SRC_PARSER=%SRC_DIR%\main\java\com\example\JsonParsery.java
set JAVA_SRC_NATIVE=%SRC_DIR%\main\java\com\example\NativeProcessor.java

REM Пути к Native исходникам
set NATIVE_SRC=%SRC_DIR%\main\native\nativeLib.cpp

REM Имя и путь к DLL
set DLL_NAME=aggregation.dll
set DLL_PATH=%BASE_DIR%
set DLL_FULL_PATH=%DLL_PATH%\%DLL_NAME%

REM Пути к JDK
set JAVA_HOME=C:\Program Files\Java\jdk-24
set JAVA_INCLUDE_PATH=%JAVA_HOME%\include
set JAVA_INCLUDE_WIN_PATH=%JAVA_HOME%\include\win32

REM Путь к MinGW
set MINGW_PATH=C:\msys64\mingw64\bin

REM Добавляем в PATH
set PATH=%MINGW_PATH%;%JAVA_HOME%\bin;%PATH%

set SRC_PATH=%SRC_DIR%\main\java
set OUT_DIR=%CLASSES_DIR%

REM Класс-пассы для сборки
set CLASSPATH=%LIBS_PATH%\gson-2.10.1.jar;%LIBS_PATH%\jackson-databind-2.15.2.jar;%LIBS_PATH%\jackson-core-2.15.2.jar;%LIBS_PATH%\jackson-annotations-2.15.2.jar

REM =====================
REM Удаление старых файлов
REM =====================
echo Удаление старых файлов и папок...

if exist "%CLASSES_DIR%" (
    rmdir /s /q "%CLASSES_DIR%"
)

if exist "%JAR_NAME%" (
    del /f /q "%JAR_NAME%"
)

if exist "%DLL_FULL_PATH%" (
    del /f /q "%DLL_FULL_PATH%"
)

REM =====================
REM Создаём папки
REM =====================
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

REM =====================
REM Компилируем файлы Java по отдельности
REM =====================

echo Компиляция Profiler.java...
javac -cp %CLASSPATH% -d "%OUT_DIR%" "%SRC_PATH%\com\example\Profiler.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Profiler.java
    goto end
)

echo Компиляция NativeProcessor.java...
javac -cp %CLASSPATH% -d "%OUT_DIR%" "%SRC_PATH%\com\example\NativeProcessor.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции NativeProcessor.java
    goto end
)

echo Компиляция DataConverter.java...
javac -cp %CLASSPATH% -d "%OUT_DIR%" "%SRC_PATH%\com\example\DataConverter.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции DataConverter.java
    goto end
)


echo Компиляция JsonParsery.java...
javac -cp %CLASSPATH% -d "%OUT_DIR%" "%SRC_PATH%\com\example\JsonParsery.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции JsonParsery.java
    goto end
)


echo Компиляция Main.java...
javac -cp "%CLASSPATH%;%OUT_DIR%" -d "%OUT_DIR%" "C:\Users\1\Documents\Practice-native\demo\src\main\java\com\example\Main.java"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Main.java
    goto end
)


REM =====================
REM Генерируем JNI заголовки
REM =====================
echo Генерация JNI заголовков...
javac -cp "%CLASSPATH%;%OUT_DIR%" -h "C:\Users\1\Documents\Practice-native\demo\src\main\native" "%JAVA_SRC_MAIN%" "%JAVA_SRC_PARSER%" "%JAVA_SRC_NATIVE%"
if %errorlevel% neq 0 (
    echo Ошибка при генерации JNI заголовков.
    goto end
)

REM =====================
REM Компиляция Native кода (.cpp) в объектный файл
REM =====================
echo Компиляция Native C++ кода...
g++ -O3  -c -I"%JAVA_INCLUDE_PATH%"  -I"C:\Users\1\Documents\Practice-native\demo\json\include" -I"%JAVA_INCLUDE_WIN_PATH%" -I"C:\msys64\mingw64\include" -o nativeLib.o "%NATIVE_SRC%"
if %errorlevel% neq 0 (
    echo Ошибка при компиляции Native C++ кода в объектный файл.
    goto end
)

REM =====================
REM Линковка в DLL
REM =====================
echo Линковка в DLL...
g++ -shared -m64 -o "%DLL_PATH%\aggregation.dll" nativeLib.o -L"C:\msys64\mingw64\lib" -lstdc++ -lws2_32
if %errorlevel% neq 0 (
    echo Ошибка при создании DLL из C++ кода.
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
echo Создаем JAR файл...
jar cfm "%JAR_NAME%" "%MANIFEST_FILE%" -C "%CLASSES_DIR%" .
if %errorlevel% neq 0 (
    echo Ошибка при создании JAR.
    goto end
)

REM =====================
REM Запуск JAR файла
REM =====================
echo Запуск JAR файла...
java --enable-native-access=ALL-UNNAMED -jar "%JAR_NAME%" -a max -f number -g name -d "C:\Users\1\Documents\Practice-native\nabor(group).json"
if %errorlevel% neq 0 (
    echo Ошибка при запуске JAR.
    goto end
)

:end
pause