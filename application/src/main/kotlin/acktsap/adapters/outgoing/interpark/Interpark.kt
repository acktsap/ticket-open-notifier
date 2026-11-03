package acktsap.adapters.outgoing.interpark

import acktsap.domain.TicketOpen
import acktsap.fp.Either
import acktsap.fp.IO
import acktsap.fp.Resource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.PersistentList
import org.openqa.selenium.By
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

private val logger = KotlinLogging.logger { }
private const val INTERPARK_URL = "https://nol.yanolja.com/ticket/display/upcoming"
private const val JS_REFRESH_MILLIS = 1_000L
private const val USER_AGENT =
    "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
private val targetZone = ZoneId.of("Asia/Seoul")

fun detectInterparkTicketOpens(): IO<List<TicketOpen>> =
    Resource.make(IO.delay(::createDriver)) { driver -> IO.delay { driver.quit() } }
        .use(::scanTicketOpens)

private fun createDriver(): ChromeDriver {
    val options =
        ChromeOptions().apply {
            addArguments("--headless=new")
            addArguments("--disable-blink-features=AutomationControlled")
            addArguments("--window-size=1920,1080")
            addArguments(USER_AGENT)
        }
    return ChromeDriver(options)
}

private fun scanTicketOpens(driver: ChromeDriver): IO<List<TicketOpen>> =
    IO.delay {
        logger.info { "Entering $INTERPARK_URL" }
        driver.get(INTERPARK_URL)
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))

        val genreButton =
            wait.until {
                driver.findElements(By.xpath("//button[contains(., '장르')]"))
                    .firstOrNull { it.isDisplayed }
            } ?: error("Cannot find genre button")
        genreButton.click()
        waitForJsRefresh()

        val musicalButton =
            wait.until {
                driver.findElements(By.xpath("//div[@role='dialog']//button[contains(., '뮤지컬')]"))
                    .firstOrNull { it.isDisplayed }
            } ?: error("Cannot find musical button")
        musicalButton.click()
        waitForJsRefresh()
        LocalDate.now(targetZone)
    }.flatMap { today -> scanPages(driver, today, ScrollProgress()) }
        .map { it.toList() }
        .tap { tickets -> IO.delay { logger.info { "Found ${tickets.size} ticket open elements" } } }

private fun scanPages(
    driver: ChromeDriver,
    today: LocalDate,
    progress: ScrollProgress,
): IO<PersistentList<TicketOpen>> =
    if (progress.complete) {
        IO.pure(progress.tickets)
    } else {
        IO.delay {
            driver.findElements(
                By.xpath(
                    "//a[contains(@href, '/ticket/products/') " +
                        "and not(ancestor::*[contains(@class, 'swiper-slide')]) " +
                        "and not(ancestor::*[contains(@class, 'pc-carousel')])]",
                ),
            ).filter { it.isDisplayed }.mapNotNull { readTicketOpen(it, today) }
        }.flatMap { detected ->
            val next = progress.record(detected)
            IO.delay {
                driver.executeScript("window.scrollBy(0, window.innerHeight * 1.5);")
                waitForJsRefresh()
            }.flatMap { scanPages(driver, today, next) }
        }
    }

private fun readTicketOpen(
    card: WebElement,
    today: LocalDate,
): TicketOpen? =
    Either.catchOrThrow<WebDriverException, TicketOpen?> {
        val time = card.findElement(By.xpath(".//span[contains(@class, 'c_semantic.text.primaryMain')]")).text.trim()
        val name = card.findElement(By.xpath(".//p[contains(@class, 'c_semantic.text.neutralMain')]")).text.trim()
        if (time.isBlank() || name.isBlank()) null else parseTicketOpen(name, time, today)
    }.fold({ null }, { it })

private fun waitForJsRefresh() {
    Thread.sleep(JS_REFRESH_MILLIS)
}
