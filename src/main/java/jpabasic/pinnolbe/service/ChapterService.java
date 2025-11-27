package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;

    public Chapter findChapter(String chapterId){
        return chapterRepository.findById(chapterId)
                .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    public Map<String,String> findChapterTitle(List<String> chapterIds){
        //String -> ObjectId 변환
        List<ObjectId> objectIds=chapterIds.stream()
                .map(ObjectId::new)
                .toList();
        //한 번에 조회
        List<Chapter> chapters=chapterRepository.findByIdIn(objectIds);
        //ObjectId -> String key 로 매핑
        return chapters.stream()
                .collect(Collectors.toMap(
                        //MongoDB의 ObjectId를 사람이 읽을 수 있는 문자열로 변환
                        c->c.getId().toHexString(),
                        Chapter::getChapterTitle
                ));

    }


    public Slice<CurrentSituationResDto.CurrentChapterRes> getAllChapters(String bookId, int page) {
        int size=6;
        //전체 chapter
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Slice<Chapter> slice = chapterRepository.findByBookId(bookId, pageable);

        return slice.map(CurrentSituationResDto.CurrentChapterRes::toDto);
    }


}
