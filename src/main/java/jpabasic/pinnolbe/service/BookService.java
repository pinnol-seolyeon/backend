package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.study.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public int getBooklevel(String bookId){
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new CustomException(ErrorCode.BOOK_NOT_FOUND));
        int bookLevel=book.getBookLevel();
        return bookLevel;
    }
}
