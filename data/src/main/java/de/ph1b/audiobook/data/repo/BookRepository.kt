package de.ph1b.audiobook.data.repo

import de.ph1b.audiobook.common.Optional
import de.ph1b.audiobook.common.orNull
import de.ph1b.audiobook.common.toOptional
import de.ph1b.audiobook.data.Book
import de.ph1b.audiobook.data.BookContent
import de.ph1b.audiobook.data.Chapter
import de.ph1b.audiobook.data.repo.internals.BookStorage
import de.ph1b.audiobook.data.repo.internals.MemoryRepo
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository
@Inject constructor(
  private val storage: BookStorage,
  private val memoryRepo: MemoryRepo
) {

  fun booksStream(): Observable<List<Book>> = memoryRepo.stream()

  fun byId(id: UUID): Observable<Optional<Book>> {
    return memoryRepo.stream().map { books ->
      books.find { it.id == id }.toOptional()
    }
  }

  suspend fun addBook(book: Book) {
    Timber.v("addBook=${book.name}")
    storage.addOrUpdate(book)
    memoryRepo.add(book)
  }

  fun bookById(id: UUID): Book? = runBlocking { memoryRepo.active().find { it.id == id } }

  suspend fun updateBookContent(content: BookContent): Book? {
    val updated = updateBookInMemory(content.id) {
      updateContent { content }
    }
    storage.updateBookContent(content)
    return updated
  }

  suspend fun getOrphanedBooks(): List<Book> = memoryRepo.orphaned()

  suspend fun activeBooks(): List<Book> = memoryRepo.active()

  suspend fun updateBook(book: Book) {
    if (bookById(book.id) == book) {
      return
    }
    if (memoryRepo.replace(book)) {
      storage.addOrUpdate(book)
    } else Timber.e("update failed as there was no book")
  }

  suspend fun updateBookName(id: UUID, name: String) {
    storage.updateBookName(id, name)
    updateBookInMemory(id) {
      updateMetaData { copy(name = name) }
    }
  }

  private suspend inline fun updateBookInMemory(id: UUID, update: Book.() -> Book): Book? {
    val book = memoryRepo.active().find { it.id == id }
    if (book == null) {
      Timber.e("update failed as there was no book")
      return null
    }
    val updatedBook = update(book)
    val replaced = memoryRepo.replace(updatedBook)
    return if (replaced) {
      updatedBook
    } else {
      Timber.e("update failed as there was no book")
      null
    }
  }

  suspend fun hideBook(toDelete: List<Book>) {
    Timber.v("hideBooks=${toDelete.size}")
    if (toDelete.isEmpty()) return
    memoryRepo.hide(toDelete)
    toDelete.forEach { storage.hideBook(it.id) }
  }

  suspend fun revealBook(book: Book) {
    Timber.v("Called revealBook=$book")
    memoryRepo.reveal(book)
    storage.revealBook(book.id)
  }

  suspend fun chapterByFile(file: File): Chapter? = memoryRepo.chapterByFile(file)
}

fun BookRepository.flowById(bookId: UUID): Flow<Book?> {
  return byId(bookId)
    .toFlowable(BackpressureStrategy.LATEST)
    .asFlow()
    .map { it.orNull }
}
