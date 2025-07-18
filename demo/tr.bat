@echo off
set JAVA_LIB_PATH=C:\Users\1\Documents\Practice-native\demo

java --enable-native-access=ALL-UNNAMED -Djava.library.path=%JAVA_LIB_PATH% -jar test.jar -a max -f number -g name -d "C:\Users\1\Documents\Practice-native\nabor(group).json"
pause