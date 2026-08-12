package jpabasic.pinnolbe.dto.study.book;

import jpabasic.pinnolbe.domain.study.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class BookListResponseDto {

    private String sessionLogId;
    private String currentBookId;
    private Integer currentBookLevel;
    private List<Map<String,String>> bookList;

    public static List<Map<String,String>> toDto(List<Book> books, Integer currentBookLevel){
        return books.stream()
                .map(book->{
                    Map<String,String> map = new HashMap<>();
                    map.put("id",book.getId().toString());
                    map.put("title",book.getTitle());
                    map.put("bookLevel",String.valueOf(book.getBookLevel()));
                    map.put("status", getStatus(book, currentBookLevel));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private static String getStatus(Book book, Integer currentBookLevel) {
        if (currentBookLevel == null) {
            return "completed";
        }
        if (book.getBookLevel() < currentBookLevel) {
            return "completed";
        }
        if (book.getBookLevel() == currentBookLevel) {
            return "in_progress";
        }
        return "locked";
    }
}
