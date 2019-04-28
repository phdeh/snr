# SyncAndRun

## Зачем

Чтобы быстро синхронизировать и выполнять программу на разных ОС, не меняя среду разработки без лишних затрат времени.

## Как использовать

`java -jar SyncAndRun.jar -r receive` — начало приёма данных в директорию `receive`.

`java -jar SyncAndRun.jar -s send -x "sh run.sh"` — отправка данных из директории `send` и исполнение комманды 
`sh run.sh` на удалённой машине с Linux/macOS.

`java -jar SyncAndRun.jar -s send -x "run.bat"` — отправка данных из директории `send` и исполнение комманды 
`run.bat` на удалённой машине с Windows.
