# Укажите путь к папке libs
$libsPath = "C:\Users\1\Documents\Practice-native\demo\libs"

# URL для скачивания Gson (последняя версия)
$gsonUrl = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"  # Замените на нужную версию

# Получите имя файла из URL
$fileName = Split-Path $gsonUrl -Leaf

# Полный путь для сохранения файла
$filePath = Join-Path $libsPath $fileName

# Создайте папку libs, если она не существует
if (!(Test-Path $libsPath)) {
    New-Item -ItemType Directory -Path $libsPath
}

# Скачайте файл
try {
    Invoke-WebRequest -Uri $gsonUrl -OutFile $filePath
    Write-Host "Gson скачан в: $filePath"
} catch {
    Write-Host "Ошибка при скачивании Gson: $($_.Exception.Message)"
}