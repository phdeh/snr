package sender

object Sender {
    var sendingPath: String = ""
    var priority: Int = 0

    operator fun invoke(sendingPath: String, priority: Int) {
        this.sendingPath = sendingPath
        this.priority = priority
    }
}
