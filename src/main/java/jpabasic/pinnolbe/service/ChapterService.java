package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;

    public Chapter findChapter(String chapterId){
        return chapterRepository.findById(chapterId)
                .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_NOT_FOUND));
    }


    public Slice<CurrentSituationResDto.CurrentChapterRes> getAllChapters(String bookId, int page) {
        int size=6;
        //전체 chapter
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Slice<Chapter> slice = chapterRepository.findByBookId(bookId, pageable);

        return slice.map(CurrentSituationResDto.CurrentChapterRes::toDto);
    }

}
