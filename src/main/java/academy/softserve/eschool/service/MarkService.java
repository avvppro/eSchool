package academy.softserve.eschool.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import academy.softserve.eschool.model.Mark;
import academy.softserve.eschool.dto.MarkDTO;
import academy.softserve.eschool.dto.MarkDataPointDTO;
import academy.softserve.eschool.repository.MarkRepository;
import academy.softserve.eschool.service.base.MarkServiceBase;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkService implements MarkServiceBase {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NonNull
    private MarkRepository markRepo;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Returns list of {@link MarkDataPointDTO}
     * @param studentId if specified marks are filtered by user id
     * @param subjectId if specified marks are filtered by subject id
     * @param classId if specified marks are filtered by class id
     * @param periodStart if specified only marks received after this date are returned
     * @param periodEnd if specified only marks received before this date are returned
     * @return list of {@link MarkDataPointDTO}
     */

    @Override
    public List<MarkDataPointDTO> getFilteredByParams(Integer subjectId, Integer classId, Integer studentId, LocalDate periodStart,
            LocalDate periodEnd) {
        
        String startDate = null;
        String endDate = null;
        
        if (periodStart != null) {
            startDate = dateFormat.format(periodStart);
        }
        if (periodEnd != null) {
            endDate = dateFormat.format(periodEnd);
        }
        return formDataPoints(markRepo.getFilteredByParamsGroupedByDate(subjectId, classId, studentId, startDate, endDate));
    }
    

    private List<MarkDataPointDTO> formDataPoints(List<Map<String, Object>> data) {
        List<MarkDataPointDTO> dataPoints;
        dataPoints = data.stream().map((obj) -> {
                double averageMark = ((BigDecimal)obj.get("avg_mark")).doubleValue();
                LocalDate date = ((Date)obj.get("date")).toLocalDate();
                int count = ((BigInteger)obj.get("count")).intValue();
                return new MarkDataPointDTO(averageMark, date, count);
            })
            .collect(Collectors.toList());
        logger.debug("Mark datapoints", dataPoints.toString());
        return dataPoints;
    }

    @Override
    public MarkDTO saveMark(MarkDTO dto) {
        logger.info("Adding mark lesson[id={}],student[id={}",dto.getIdLesson(),dto.getIdStudent());
        markRepo.saveMarkByLesson(dto.getIdStudent(),dto.getIdLesson(),dto.getMark(),dto.getNote());
        Mark mark = markRepo.findTopByStudentIdAndLessonId(dto.getIdStudent(),dto.getIdLesson());
        MarkDTO markDTO = MarkDTO.builder()
                .idMark(mark.getId())
                .mark(mark.getMark())
                .idLesson(mark.getLesson().getId())
                .idStudent(mark.getStudent().getId())
                .note(mark.getNote())
                .build();
        return markDTO;
    }

    @Override
    public void updateType(int idLesson, String markType) {
        logger.info("Editing markType[{}] lesson[id={}]",markType,idLesson);
        markRepo.saveTypeByLesson(idLesson,markType);
    }
}
