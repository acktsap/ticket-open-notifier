package acktsap.detector

import acktsap.model.Platform
import acktsap.model.TicketOpen
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class InterparkTicketOpenDetector : TicketOpenDetector {
    override fun detect(): List<TicketOpen> {
        val options =
            ChromeOptions().apply {
                // headless option
                // comment out if you want to show bahavior
                addArguments("--headless=new")
            }
        val driver = ChromeDriver(options)
        return try {
            driver.get(URL)
            val tableBody =
                driver.findElement(By.cssSelector("body > div > div > div.list > div.table > table > tbody"))
            val rows = tableBody.findElements(By.tagName("tr"))
            if (rows.size == 1 && isNotFound(rows.first())) {
                listOf()
            } else {
                rows.map { convertToTicketOpen(it) }
            }
        } catch (e: Exception) {
            throw IllegalStateException(e)
        } finally {
            driver.quit()
        }
    }

    private fun isNotFound(element: WebElement): Boolean {
        val notFoundTableData = element.findElements(By.cssSelector("not_found"))
        return notFoundTableData.isEmpty()
    }

    private fun convertToTicketOpen(tableRow: WebElement): TicketOpen {
        val subjectTd = tableRow.findElement(By.cssSelector("td.subject"))
        val dateTd = tableRow.findElement(By.cssSelector("td.date"))
        val dateTime = LocalDateTime.parse(dateTd.text, DATE_FORMAT)

        return TicketOpen(
            name = subjectTd.text,
            platform = Platform.INTERPARK,
            dateTime = dateTime,
        )
    }

    companion object {
        @Suppress("ktlint:standard:max-line-length")
        private const val URL =
            "https://ticket.interpark.com/webzine/paper/TPNoticeList_iFrame.asp?bbsno=34&pageno=1&KindOfGoods=TICKET&Genre=1&sort=opendate&stext="

        // format : 24.09.25(수) 14:00
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yy.MM.dd(EEE) HH:mm", Locale.KOREAN)
    }
}
