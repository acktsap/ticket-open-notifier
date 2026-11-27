package acktsap.detector

import acktsap.model.Platform
import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.DayOfWeek
import java.time.Duration
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
                addArguments("--disable-blink-features=AutomationControlled")
                addArguments("--window-size=1920,1080")
                addArguments(USER_AGENT)
            }
        val driver = ChromeDriver(options)
        try {
            logger.info { "Entering $URL" }
            driver.get(URL)

            val wait = WebDriverWait(driver, Duration.ofSeconds(10))

            // 장르 필터 버튼 클릭
            val genreButton =
                wait.until {
                    val btns = driver.findElements(By.xpath("//button[contains(., '장르')]"))
                    btns.firstOrNull { it.isDisplayed }
                } ?: throw IllegalStateException("Cannot find genre button")
            genreButton.click()
            waitForJsRefresh()

            // 팝업 모달/바텀시트(role='dialog')에서 '뮤지컬' 선택
            val musicalButton =
                wait.until {
                    val btns = driver.findElements(By.xpath("//div[@role='dialog']//button[contains(., '뮤지컬')]"))
                    btns.firstOrNull { it.isDisplayed }
                } ?: throw IllegalStateException("Cannot find musical button")
            musicalButton.click()
            waitForJsRefresh()

            val tickOpenSet = mutableSetOf<TicketOpen>()
            var lastCount = 0
            var noNewCount = 0
            while (noNewCount < 3) {
                val ticketCards =
                    driver.findElements(
                        By.xpath(
                            "//a[contains(@href, '/ticket/products/') " +
                                "and not(ancestor::*[contains(@class, 'swiper-slide')]) " +
                                "and not(ancestor::*[contains(@class, 'pc-carousel')])]",
                        ),
                    )

                for (card in ticketCards) {
                    if (!card.isDisplayed) continue
                    val ticketOpen = extractTickOpen(card)
                    if (ticketOpen != null) {
                        tickOpenSet.add(ticketOpen)
                    }
                }

                if (tickOpenSet.size == lastCount) {
                    noNewCount++
                } else {
                    noNewCount = 0
                    lastCount = tickOpenSet.size
                }

                driver.executeScript("window.scrollBy(0, window.innerHeight * 1.5);")
                waitForJsRefresh()
            }

            logger.info { "Found ${tickOpenSet.size} ticket open elements" }
            return tickOpenSet.toList()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        } finally {
            driver.quit()
        }
    }

    private fun extractTickOpen(cardElement: WebElement): TicketOpen? {
        return try {
            val ticketOpenTime = cardElement.findElement(By.xpath(".//span[contains(@class, 'c_semantic.text.primaryMain')]")).text.trim()
            val target = cardElement.findElement(By.xpath(".//p[contains(@class, 'c_semantic.text.neutralMain')]")).text.trim()
            if (ticketOpenTime.isEmpty() || target.isEmpty()) {
                return null
            }
            logger.trace { "Parsing result (ticketOpenTime: $ticketOpenTime, target: $target)" }

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
            logger.trace(e) { "Failed to parse ticket open time (expected format: $DATE_FORMAT)" }
            null
        } catch (e: Exception) {
            logger.trace(e) { "Failed to parse ticket open card" }
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
        Thread.sleep(SLEEP_INTERVAL_MS)
    }

    companion object {
        private const val URL = "https://nol.yanolja.com/ticket/display/upcoming"
        private const val SLEEP_INTERVAL_MS = 1000L

        private const val USER_AGENT =
            "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

        private val TARGET_ZONE_ID = ZoneId.of("Asia/Seoul")

        // format : 2025.09.25(수) 14:00
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd(EEE) HH:mm", Locale.KOREAN)
    }
}
