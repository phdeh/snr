import conventions.ConsoleKey
import conventions.CrashCode
import receiver.Receiver
import sender.Sender
import kotlin.concurrent.thread

/*

 Поведение в режиме клиента:

    * Найти принимающий файлы сервер.
    * Рекурсивно скопировать отправляемую папку во временную директорию.
    * Заархивировать временную директорию.
    * Отправить временную директорию.
    * Отображать консольный вывод сервера.

 Поведение в режиме сервера:

    * Ждать клиента.
    * Принять от клиента архив.
    * Распаковать архив в директорию.
    * Найти в директории файл compile.sh/compile.bat и исполнить, отправляя вывод клиенту.
    * Найти в директории файл executable/executable.exe и исполнить, отправляя вывод клиенту.
    * В случае прерывания связи с клиентом, убить процесс, вернуться к ожиданию клиента.

 */

fun main(args: Array<String>) {
    val keys = ConsoleKey.from(args)

    fun handleIfNotNull(key: ConsoleKey, action: (List<String>) -> Unit) {
        val list = keys[key]
        if (list != null)
            action(list)
    }

    handleIfNotNull(ConsoleKey.HELP) {
        println("Commands:")
        println()
        ConsoleKey.values().forEach {
            println("   ${it.shortKey} ${it.longKey} ${it.help}")
            println()
        }
    }

    var priority = 0

    handleIfNotNull(ConsoleKey.PRIORITY) {
        priority = Integer.valueOf(priority)
    }

    handleIfNotNull(ConsoleKey.RECEIVE) {
        thread { Receiver(it[0], priority) }
    }

    handleIfNotNull(ConsoleKey.SEND) {
        thread { Sender(it[0], priority) }
    }

}