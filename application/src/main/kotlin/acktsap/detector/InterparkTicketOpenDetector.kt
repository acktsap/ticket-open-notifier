package acktsap.detector

import acktsap.model.Platform
import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val logger = KotlinLogging.logger { }

class InterparkTicketOpenDetector : TicketOpenDetector {
    override fun detect(): List<TicketOpen> {
        val options =
            ChromeOptions().apply {
                // headless option
                // comment out if you want to show behavior
                addArguments("--headless=new")
            }
        val driver = ChromeDriver(options)
        try {
            driver.get(URL)

            val typeButtonElement = driver.findElement(By.xpath("//button[text()=\"장르\"]"))
            typeButtonElement.click()
            waitForJsRefresh()

            val musicalTypeButtonElement = driver.findElement(By.xpath("//button[text()=\"뮤지컬\"]"))
            musicalTypeButtonElement.click()
            waitForJsRefresh()

            val openListDivElement = driver.findElement(By.xpath("//*[@aria-label=\"상품 리스트\"]"))
            val tickOpenSet = mutableSetOf<TicketOpen>()
            var lastPosition: Number? = null
            while (true) {
                val ticketOpenLinkElements = openListDivElement.findElements(By.tagName("a"))
                logger.trace { "Found ${ticketOpenLinkElements.size} ticket open link elements" }

                val tickOpens =
                    ticketOpenLinkElements
                        .mapNotNull { extractTickOpen(it) }
                tickOpenSet.addAll(tickOpens)

                // scroll one page
                driver.executeScript("window.scrollTo(0, $lastPosition + window.innerHeight);")

                val currentPosition = driver.executeScript("return window.scrollY;") as Number
                logger.trace { "Page location detection (lastPosition: $lastPosition, currentPosition: $currentPosition)" }
                if (lastPosition == currentPosition) {
                    logger.trace { "Page is in the bottom. Exit the loop" }
                    break
                }
                lastPosition = currentPosition

                waitForJsRefresh()
            }

            return tickOpenSet.toList()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        } finally {
            driver.quit()
        }
    }

    private fun extractTickOpen(openListDivElement: WebElement): TicketOpen? {
        val unorderedListElement = openListDivElement.findElement(By.tagName("ul"))

        val listItemElements = unorderedListElement.findElements(By.tagName("li"))
        val (ticketOpenTimeElement, targetElement) = listItemElements
        val ticketOpenTime = ticketOpenTimeElement.text
        val target = targetElement.text
        logger.trace { "Parsing result (ticketOpenTime: $ticketOpenTime, target: $target)" }

        return try {
            val formattedOpenTime =
                when {
                    ticketOpenTime.startsWith("오늘 ") -> formatTodayOpenTime(ticketOpenTime.removePrefix("오늘 "))
                    ticketOpenTime.startsWith("내일 ") -> formatTomorrowOpenTime(ticketOpenTime.removePrefix("내일 "))
                    else -> formatOtherOpenTime(ticketOpenTime)
                }

            val parsedLocalDate = LocalDateTime.parse(formattedOpenTime, DATE_FORMAT)
            TicketOpen(
                name = target,
                platform = Platform.INTERPARK,
                dateTime = parsedLocalDate,
            )
        } catch (e: DateTimeParseException) {
            logger.trace(e) { "Failed to parse $ticketOpenTime (expected format: $DATE_FORMAT)" }
            null
        }
    }

    // 오늘 13:00
    private fun formatTodayOpenTime(rawTicketOpenTime: String): String {
        val today = LocalDate.now(TARGET_ZONE_ID)
        val year = today.year
        val month = formatMonth(today.monthValue)
        val dayOfMonth = formatDayOfWeek(today.dayOfMonth)
        val dayOfWeek = formatDayOfWeekInHangul(today.dayOfWeek)

        return "$year.$month.$dayOfMonth($dayOfWeek) $rawTicketOpenTime"
    }

    // 내일 13:00
    private fun formatTomorrowOpenTime(rawTicketOpenTime: String): String {
        val tomorrow = LocalDate.now(TARGET_ZONE_ID).plusDays(1)
        val year = tomorrow.year
        val month = formatMonth(tomorrow.monthValue)
        val dayOfMonth = formatDayOfWeek(tomorrow.dayOfMonth)
        val dayOfWeek = formatDayOfWeekInHangul(tomorrow.dayOfWeek)

        return "$year.$month.$dayOfMonth($dayOfWeek) $rawTicketOpenTime"
    }

    // 04.21(월) 10:00
    private fun formatOtherOpenTime(rawTicketOpenTime: String): String {
        val year = LocalDate.now(TARGET_ZONE_ID).year
        return "$year.$rawTicketOpenTime"
    }

    private fun formatMonth(target: Int): String {
        return if (target < 10) {
            "0$target"
        } else {
            target.toString()
        }
    }

    private fun formatDayOfWeek(target: Int): String {
        return if (target < 10) {
            "0$target"
        } else {
            target.toString()
        }
    }

    private fun formatDayOfWeekInHangul(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "월"
            DayOfWeek.TUESDAY -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY -> "목"
            DayOfWeek.FRIDAY -> "금"
            DayOfWeek.SATURDAY -> "토"
            DayOfWeek.SUNDAY -> "일"
        }
    }

    private fun waitForJsRefresh() {
        Thread.sleep(500L)
    }

    companion object {
        private const val URL = "https://tickets.interpark.com/contents/notice"

        private val TARGET_ZONE_ID = ZoneId.of("Asia/Seoul")

        // format : 2025.09.25(수) 14:00
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd(EEE) HH:mm", Locale.KOREAN)
    }
}
