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

    private String currentBookId;
    private List<Map<String,String>> bookList;

    public static List<Map<String,String>> toDto(List<Book> books){
        return books.stream()
                .map(book->{
                    Map<String,String> map = new HashMap<>();
                    map.put("id",book.getId().toString());
                    map.put("title",book.getTitle());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
