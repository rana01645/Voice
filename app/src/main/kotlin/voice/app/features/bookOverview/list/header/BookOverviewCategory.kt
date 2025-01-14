package voice.app.features.bookOverview.list.header

import androidx.annotation.StringRes
import voice.app.R
import voice.data.Book
import voice.data.BookComparator
import java.util.concurrent.TimeUnit.SECONDS

enum class BookOverviewCategory(
  @StringRes val nameRes: Int,
  val comparator: Comparator<Book>
) {
  CURRENT(
    nameRes = R.string.book_header_current,
    comparator = BookComparator.ByLastPlayed
  ),
  NOT_STARTED(
    nameRes = R.string.book_header_not_started,
    comparator = BookComparator.ByDateAdded
  ),
  FINISHED(
    nameRes = R.string.book_header_completed,
    comparator = BookComparator.ByLastPlayed
  );

  val filter: (Book) -> Boolean = { it.category == this }
}

val Book.category: BookOverviewCategory
  get() {
    return if (position == 0L) {
      BookOverviewCategory.NOT_STARTED
    } else {
      if (position > duration - SECONDS.toMillis(1)) {
        BookOverviewCategory.FINISHED
      } else {
        BookOverviewCategory.CURRENT
      }
    }
  }
