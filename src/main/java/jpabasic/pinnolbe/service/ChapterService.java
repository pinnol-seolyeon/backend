package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.QuizItem;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    public void saveQuizFromExcel(String chapterId, MultipartFile file){
        List<QuizItem> quizList=parseExcel(file);

        Chapter chapter=chapterRepository.findById(chapterId)
            .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_NOT_FOUND));

        chapter.setQuizzes(quizList);
        chapterRepository.save(chapter);
    }

    private List<QuizItem> parseExcel(MultipartFile file) {

        List<QuizItem> quizzes = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 1행은 header라 skip
                Row row = sheet.getRow(i);

                String quiz = getValue(row.getCell(0));
                String option1 = getValue(row.getCell(1));
                String option2 = getValue(row.getCell(2));
                String option3 = getValue(row.getCell(3));
                String option4 = getValue(row.getCell(4));
                String answer = getValue(row.getCell(5));

                quizzes.add(
                    QuizItem.builder()
                        .id(UUID.randomUUID().toString())
                        .quiz(quiz)
                        .options(List.of(option1, option2, option3, option4))
                        .answer(answer)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("엑셀 파싱 오류", e);
        }

        return quizzes;
    }

    private String getValue(Cell cell) {
        if (cell == null) return "";
        return cell.toString().trim();
    }

}
